package com.muallimsaniqr.flutter_app.ui.screens.downloading

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringArrayResource
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.muallimsaniqr.flutter_app.common.base.BaseVM
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.utils.constants.CommonConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class FileDownloaderVM @Inject constructor(
    private val localStorage: AppPreference
) : BaseVM() {

    var isDownloading = mutableStateOf(true)
    val progress = mutableIntStateOf(0)
    private var downloadCount = 0
    private var itemsCount = 0

    fun downloadVideos(videoFolder: File) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        try {
            viewModelScope.launch(Dispatchers.IO) {
                storage.reference.child(CommonConstants.FIREBASE_VIDEOS_FOLDER).listAll()
                    .addOnSuccessListener {
                        itemsCount = it.items.size
                        initializeAlreadyDownloaded()
                        it.items.forEach { item ->
                            val name = item.name
                            if (!isFileAlreadyDownloaded(name)) {
                                val videoFile = File(videoFolder, name)
                                item.getFile(videoFile).addOnSuccessListener {
                                    updateDownloadProgress()
                                    setFileAsDownloaded(name)
                                    Log.e("SUCCESSdownloadVideo", "downloadVideo: $name")
                                }.addOnFailureListener {
                                    Log.e("FAILUREdownloadVideo", "downloadVideo: $it")
                                }
                            }
                        }
                    }.addOnFailureListener {
                    Log.e("failureOnGetVideos", "$it")
                }
            }
        } catch (e: Exception) {
            Log.e("exceptionOnDownloadVideo", "downloadVideoArchive: $e")
        }
    }

    private fun initializeAlreadyDownloaded() {
        val alreadyDownloadedFiles = localStorage.getAlreadyDownloadedVideos()
        Log.e("alreadyDownloadedFiles", "$alreadyDownloadedFiles")
        downloadCount = alreadyDownloadedFiles.size
        updateUiProgress()
        checkIfAllFilesDownloaded()
    }

    private fun updateDownloadProgress() {
        downloadCount++
        updateUiProgress()
        checkIfAllFilesDownloaded()
    }

    private fun updateUiProgress() {
        progress.intValue = if (itemsCount == 0) 0 else (downloadCount * 100) / itemsCount
        Log.e("updateUiProgress", "downloadCount: $downloadCount, itemsCount: $itemsCount and progress: ${progress.intValue}")
    }

    private fun checkIfAllFilesDownloaded() {
        if (downloadCount == itemsCount) {
            localStorage.setFilesDownloaded(true)
            isDownloading.value = false
        }
    }

    private fun isFileAlreadyDownloaded(fileName: String): Boolean {
        val alreadyDownloadFiles = localStorage.getAlreadyDownloadedVideos()
        Log.e("isAlreadyDownloadFile", "fileName $fileName in $alreadyDownloadFiles ${fileName in alreadyDownloadFiles}")
        return fileName in alreadyDownloadFiles
    }

    private fun setFileAsDownloaded(fileName: String) {
        localStorage.setAlreadyDownloadedVideo(fileName)
    }


// Use to download via DownloadManager to show notification
//     suspend fun downloadVideo(uri: Uri, name: String, context: Context) {
//        if (isFileAlreadyDownloaded(name)) return
//
//        val request = DownloadManager.Request(uri)
//            .setTitle(name)
//            .setDescription("Downloading")
//            .setAllowedOverRoaming(false)
//            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .setDestinationInExternalFilesDir(
//                context,
//                CommonConstants.VIDEOS_DIRECTION,
//                name
//            )
//        val downloadManager = context.getSystemService(DownloadManager::class.java)
//        downloadManager.enqueue(request)
//
//    }
}


data class DonwloadingFile(val uri: Uri, val name: String)