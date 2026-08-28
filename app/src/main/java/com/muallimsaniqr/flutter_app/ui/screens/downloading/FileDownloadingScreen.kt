package com.muallimsaniqr.flutter_app.ui.screens.downloading

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.ui.extensions.getDownloadedVideoFolder
import com.muallimsaniqr.flutter_app.ui.extensions.painter
import com.muallimsaniqr.flutter_app.ui.extensions.showToast
import com.muallimsaniqr.flutter_app.ui.extensions.strResource
import com.muallimsaniqr.flutter_app.ui.widgets.buttons.MualimButton

@Composable
fun FileDownloadingScreen(vm: FileDownloaderVM, navigateToHomeScreen: () -> Unit = {}) {

    val context = LocalContext.current
    val videoFolder = remember { context.getDownloadedVideoFolder() }
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.downloadVideos(videoFolder)
    }

    val allDownloadedMessage = R.string.all_files_downloaded.strResource
    LaunchedEffect(state) {
        if (state is DownloadState.Completed) {
            context.showToast(allDownloadedMessage)
            navigateToHomeScreen()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {

            Image(
                painter = R.drawable.ic_logo.painter,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val current = state) {
                    is DownloadState.Failed -> {
                        Text(
                            text = R.string.download_failed.strResource,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        MualimButton(text = R.string.retry.strResource) {
                            vm.retry(videoFolder)
                        }
                    }

                    else -> {
                        val percent = (current as? DownloadState.InProgress)?.percent ?: 0
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { percent / 100F },
                                modifier = Modifier.size(56.dp),
                            )
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = R.string.downloading.strResource,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
