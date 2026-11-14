package com.muallimsaniqr.flutter_app.ui.screens.downloading

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.muallimsaniqr.flutter_app.BuildConfig
import com.muallimsaniqr.flutter_app.common.base.BaseVM
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.data.videos.RemoteVideo
import com.muallimsaniqr.flutter_app.data.videos.VideoDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

/** Ders videolarinin indirilme durumu. */
sealed interface DownloadState {
    /** Sunucudan video listesi (manifest.json) aliniyor. */
    data object Preparing : DownloadState

    /** Indirme suruyor. [percent] 0..100 arasindadir ve indirilen bayta gore hesaplanir. */
    data class InProgress(val percent: Int, val downloaded: Int, val total: Int) : DownloadState

    /** Butun dosyalar indirildi. */
    data object Completed : DownloadState

    /** Indirme basarisiz oldu; kullaniciya tekrar deneme sunulur. */
    data object Failed : DownloadState
}

@HiltViewModel
class FileDownloaderVM @Inject constructor(
    private val localStorage: AppPreference
) : BaseVM() {

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Preparing)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    private val downloader = VideoDownloader(BuildConfig.VIDEOS_BASE_URL)
    private var downloadJob: Job? = null

    /**
     * `manifest.json` icindeki butun ders videolarini [videoFolder] icine indirir.
     *
     * Diskte dogru boyutta duran dosyalar atlanir. Indirme sirasinda gecici `.part`
     * dosyasi kullanilir; baglanti koparsa bir sonraki denemede kaldigi yerden devam
     * eder. En fazla [MAX_PARALLEL_DOWNLOADS] dosya ayni anda indirilir.
     */
    fun downloadVideos(videoFolder: File) {
        if (downloadJob?.isActive == true) return

        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            _state.value = DownloadState.Preparing
            runCatching {
                val videos = withRetry("manifest.json") { downloader.fetchManifest() }.videos
                check(videos.isNotEmpty()) { "manifest.json bos" }
                videos.forEach { require(SAFE_NAME.matches(it.name)) { "Gecersiz dosya adi: ${it.name}" } }

                videoFolder.mkdirs()
                val totalBytes = videos.sumOf { it.size }.coerceAtLeast(1)
                val doneBytes = AtomicLong(0)
                val doneFiles = AtomicInteger(0)
                fun publish() {
                    val percent = (doneBytes.get() * 100 / totalBytes).toInt().coerceIn(0, 100)
                    _state.value = DownloadState.InProgress(percent, doneFiles.get(), videos.size)
                }
                publish()

                val semaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)
                coroutineScope {
                    videos.map { video ->
                        async {
                            semaphore.withPermit {
                                downloadIfNeeded(video, videoFolder) { bytes ->
                                    doneBytes.addAndGet(bytes)
                                    publish()
                                }
                            }
                            doneFiles.incrementAndGet()
                            publish()
                        }
                    }.awaitAll()
                }

                localStorage.setFilesDownloaded(true)
                _state.value = DownloadState.Completed
            }.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Log.e(TAG, "Videolar indirilemedi", error)
                _state.value = DownloadState.Failed
            }
        }
    }

    /** Hata sonrasi kullanicinin "tekrar dene" aksiyonu. */
    fun retry(videoFolder: File) {
        downloadJob?.cancel()
        downloadJob = null
        downloadVideos(videoFolder)
    }

    private suspend fun downloadIfNeeded(video: RemoteVideo, videoFolder: File, onBytes: (Long) -> Unit) {
        val target = File(videoFolder, video.name)

        // Boyut kontrolu sayesinde hem silinmis hem de sunucuda guncellenmis videolar yeniden iner.
        if (target.exists() && target.length() == video.size) {
            onBytes(video.size)
            return
        }

        val partFile = File(videoFolder, "${video.name}.part")
        var counted = 0L
        withRetry(video.name) {
            // Basarisiz denemede sayilan baytlari geri al; resume, mevcut kismi tekrar sayar.
            onBytes(-counted)
            counted = 0
            downloader.download(video, partFile) { bytes ->
                counted += bytes
                onBytes(bytes)
            }
        }

        target.delete()
        check(partFile.renameTo(target)) { "Gecici dosya tasinamadi: ${video.name}" }
        localStorage.setAlreadyDownloadedVideo(video.name)
    }

    private suspend fun <T> withRetry(label: String, block: () -> T): T {
        var lastError: Throwable? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            coroutineContext.ensureActive()
            try {
                return block()
            } catch (e: java.io.IOException) {
                lastError = e
                Log.w(TAG, "$label: deneme ${attempt + 1}/$MAX_ATTEMPTS basarisiz", e)
                if (attempt < MAX_ATTEMPTS - 1) delay(RETRY_DELAY_MS * (attempt + 1))
            }
        }
        throw lastError!!
    }

    private companion object {
        const val TAG = "FileDownloaderVM"
        const val MAX_PARALLEL_DOWNLOADS = 4
        const val MAX_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 2_000L
        val SAFE_NAME = Regex("[A-Za-z0-9._-]+")
    }
}
