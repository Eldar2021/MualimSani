package com.muallimsaniqr.flutter_app.ui.screens.downloading

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.muallimsaniqr.flutter_app.common.base.BaseVM
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.data.videos.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/** Ders videolarinin toplu indirilme durumu. */
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

/**
 * Yan menudeki "tum dersleri indir" ekrani: internetsiz kullanim icin butun videolari
 * bir kerede indirir. Normal akista videolar ders acildikca tek tek iner (bkz. HomeVM).
 */
@HiltViewModel
class FileDownloaderVM @Inject constructor(
    private val videos: VideoRepository,
    private val localStorage: AppPreference,
) : BaseVM() {

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Preparing)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    private var downloadJob: Job? = null

    /**
     * `manifest.json` icindeki butun videolari indirir. Cihazda dogru boyutta duran
     * dosyalar atlanir; en fazla [MAX_PARALLEL_DOWNLOADS] dosya ayni anda indirilir.
     */
    fun downloadAll() {
        if (downloadJob?.isActive == true) return

        downloadJob = viewModelScope.launch {
            _state.value = DownloadState.Preparing
            try {
                val list = videos.manifest().videos
                val totalBytes = list.sumOf { it.size }.coerceAtLeast(1)
                val doneBytes = AtomicLong(0)
                val doneFiles = AtomicInteger(0)
                fun publish() {
                    val percent = (doneBytes.get() * 100 / totalBytes).toInt().coerceIn(0, 100)
                    _state.value = DownloadState.InProgress(percent, doneFiles.get(), list.size)
                }
                publish()

                val semaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)
                coroutineScope {
                    list.map { video ->
                        async {
                            semaphore.withPermit {
                                videos.download(video) { bytes ->
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Videolar indirilemedi", e)
                _state.value = DownloadState.Failed
            }
        }
    }

    /** Hata sonrasi kullanicinin "tekrar dene" aksiyonu. */
    fun retry() {
        downloadJob?.cancel()
        downloadJob = null
        downloadAll()
    }

    private companion object {
        const val TAG = "FileDownloaderVM"
        const val MAX_PARALLEL_DOWNLOADS = 4
    }
}
