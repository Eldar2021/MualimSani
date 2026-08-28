package com.muallimsaniqr.flutter_app.ui.screens.downloading


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.ui.extensions.getDownloadedVideoFolder
import com.muallimsaniqr.flutter_app.ui.extensions.painter
import com.muallimsaniqr.flutter_app.ui.extensions.showToast
import com.muallimsaniqr.flutter_app.ui.extensions.strResource

@Composable
fun FileDownloadingScreen(vm: FileDownloaderVM, navigateToHomeScreen: () -> Unit = {}) {

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        val videoFolder = context.getDownloadedVideoFolder()
        vm.downloadVideos(videoFolder)
    }

    Scaffold(
        content = {
            Box(modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
            ) {

                Image(painter = R.drawable.ic_logo.painter, contentDescription = "logo", modifier = Modifier.size(80.dp).align(Alignment.Center))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 16.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = vm.progress.intValue / 100F,
                            modifier = Modifier.size(56.dp),
                        )
                        Text(text = "${vm.progress.intValue}%", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    }
                    Text(
                        text = R.string.downloading.strResource,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

            }

            val allDownloadedMessage = R.string.all_files_downloaded.strResource
            if (!vm.isDownloading.value) {
                LaunchedEffect(key1 = vm.isDownloading.value) {
                    navigateToHomeScreen()
                    context.showToast(allDownloadedMessage)
                }
            }
        }
    )

}