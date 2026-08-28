package com.muallimsaniqr.flutter_app.ui.screens.downloading

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muallimsaniqr.flutter_app.BuildConfig
import com.muallimsaniqr.flutter_app.common.base.BaseVM
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/** Ders videolarinin indirilme durumu. */
sealed interface DownloadState {
    /** Firebase'den dosya listesi aliniyor. */
    data object Preparing : DownloadState

    /** Indirme suruyor. [percent] 0..100 arasindadir. */
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

    private var downloadJob: Job? = null

    /**
     * Firebase Storage'daki butun ders videolarini [videoFolder] icine indirir.
     *
     * Zaten indirilmis dosyalar atlanir. Indirme sirasinda gecici `.part` dosyasi
     * kullanilir; boylece yarim kalan bir indirme bir sonraki acilista bozuk video
     * olarak gorunmez. En fazla [MAX_PARALLEL_DOWNLOADS] dosya ayni anda indirilir.
     */
    fun downloadVideos(videoFolder: File) {
        if (downloadJob?.isActive == true) return

        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            _state.value = DownloadState.Preparing
            runCatching {
                val folderRef = FirebaseStorage.getInstance()
                    .reference
                    .child(BuildConfig.FIREBASE_VIDEOS_FOLDER)

                val items = Tasks.await(folderRef.listAll()).items
                check(items.isNotEmpty()) { "Firebase Storage klasoru bos: ${BuildConfig.FIREBASE_VIDEOS_FOLDER}" }

                val total = items.size
                val completed = AtomicInteger(0)
                _state.value = DownloadState.InProgress(0, 0, total)

                val semaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)
                coroutineScope {
                    items.map { item ->
                        async {
                            semaphore.withPermit { downloadIfNeeded(item, videoFolder) }
                            val done = completed.incrementAndGet()
                            _state.value = DownloadState.InProgress(done * 100 / total, done, total)
                        }
                    }.awaitAll()
                }

                localStorage.setFilesDownloaded(true)
                _state.value = DownloadState.Completed
            }.onFailure { error ->
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

    private fun downloadIfNeeded(item: StorageReference, videoFolder: File) {
        val name = item.name
        val target = File(videoFolder, name)

        // Hem kayit hem de dosyanin diskte gercekten var oldugu kontrol edilir:
        // kullanici uygulama verilerini silmis olabilir.
        val alreadyDownloaded = name in localStorage.getAlreadyDownloadedVideos() &&
                target.exists() && target.length() > 0
        if (alreadyDownloaded) return

        val partFile = File(videoFolder, "$name.part")
        partFile.delete()
        Tasks.await(item.getFile(partFile))

        target.delete()
        check(partFile.renameTo(target)) { "Gecici dosya tasinamadi: $name" }
        localStorage.setAlreadyDownloadedVideo(name)
    }

    private companion object {
        const val TAG = "FileDownloaderVM"
        const val MAX_PARALLEL_DOWNLOADS = 4
    }
}
