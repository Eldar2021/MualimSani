package com.muallimsaniqr.flutter_app.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.R.*
import com.muallimsaniqr.flutter_app.domain.model.HeaderItem
import com.muallimsaniqr.flutter_app.domain.model.Language
import com.muallimsaniqr.flutter_app.domain.model.Lesson
import com.muallimsaniqr.flutter_app.ui.extensions.getDownloadedVideoFolder
import com.muallimsaniqr.flutter_app.ui.extensions.painter
import com.muallimsaniqr.flutter_app.ui.extensions.showToast
import com.muallimsaniqr.flutter_app.ui.extensions.strResource
import com.muallimsaniqr.flutter_app.ui.screens.scanner.ScannerActivity
import com.muallimsaniqr.flutter_app.ui.widgets.SelectLanguage
import com.muallimsaniqr.flutter_app.ui.widgets.buttons.MualimButton
import com.muallimsaniqr.flutter_app.ui.widgets.headers.Header
import com.muallimsaniqr.flutter_app.ui.widgets.toolbars.NavigatedToolbar
import com.muallimsaniqr.flutter_app.utils.language.LocaleUtils
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.uri.VideoPlayerMediaItem
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    vm: HomeVM = hiltViewModel(),
    lang: Language = Language.KYRGYZ,
    openOnBoarding: () -> Unit = {},
    onLanguageChanged: (Language) -> Unit = {}
) {

    var context = LocalContext.current

    var fileNameExceptVideoExt by remember {
        mutableIntStateOf(0)
    }
    var videoUri: Uri? by remember {
        mutableStateOf(null)
    }

    var isOnFullScreen by remember {
        mutableStateOf(false)
    }

    val lazyColumnState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            val data: Intent? = it.data
            val result = data?.getStringExtra(ScannerActivity.RESULT_SUCCESS)
            result?.let {
                fileNameExceptVideoExt = vm.getFileNameExceptExtByScannerResult(it)
            }
        })

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    SelectLanguage(
                        language = lang,
                        onLanguageSelected = {
                            LocaleUtils.setLocale(context, it)
                            onLanguageChanged(it)
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                    )
                    Divider(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 20.dp))
                    MualimButton(text = R.string.buy_book.strResource, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        openOnBoarding()
                    }
                }
            }
        ) {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            scannerLauncher.launch(Intent(context, ScannerActivity::class.java))
                        },
                        modifier = Modifier.padding(bottom = 12.dp, end = 4.dp)
                    ) {
                        Icon(
                            painter = drawable.ic_qr.painter,
                            contentDescription = "QR",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                topBar = {
                    NavigatedToolbar(
                        title = stringResource(string.app_name),
                        icon = R.drawable.ic_hamburger,
                        textAlignment = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        onNavigatedIconClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
            ) { paddings ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddings)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {


                            if (fileNameExceptVideoExt != 0) {
                                vm.setScannedId(fileNameExceptVideoExt)
                                SideEffect {
                                    coroutineScope.launch {
                                        lazyColumnState.scrollToItem(
                                            vm.getLessonIndexByFileName(
                                                fileNameExceptVideoExt
                                            )
                                        )
                                    }
                                }
                                videoUri =
                                    if (fileNameExceptVideoExt != 0) context.getDownloadedVideoFolder()
                                        .resolve("$fileNameExceptVideoExt.mp4").toUri() else null
                                videoUri?.let {
                                    VideoScreen(
                                        fileName = it,
                                        onFullScreenEnter = {
                                            isOnFullScreen = true
                                        },
                                        onFullScreenExit = {
                                            isOnFullScreen = false
                                        }
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 10.dp, end = 10.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7F
                                            )
                                        )
                                        .clickable { openOnBoarding() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = R.string.buy_book.strResource,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                    Icon(
                                        painter = R.drawable.ic_info.painter,
                                        contentDescription = "Info",
                                        modifier = Modifier.padding(start = 4.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = drawable.ic_qr.painter,
                                        modifier = Modifier
                                            .size(60.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        contentDescription = "QR"
                                    )
                                    Text(
                                        text = stringResource(string.tap_to_open_video),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                }
                            }

                        }


                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            state = lazyColumnState
                        ) {
                            items(vm.items.size) {
                                val item = vm.items[it]
                                when (item) {
                                    is HeaderItem -> {
                                        Header(
                                            text = item.title.strResource,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }

                                    is Lesson -> {
                                        val backgroundColor = if (fileNameExceptVideoExt == item.fileNameExceptExt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
                                        val textColor = if (fileNameExceptVideoExt == item.fileNameExceptExt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        Text(
                                            text = item.title.strResource,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(backgroundColor)
                                                .clickable {
                                                    if (vm.checkBeforeScanned(item.id)) fileNameExceptVideoExt =
                                                        item.fileNameExceptExt
                                                    else context.showToast(R.string.open_lesson_hint)
                                                }
                                                .padding(8.dp),
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isOnFullScreen) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

    }



}


@Composable
fun VideoScreen(fileName: Uri, modifier: Modifier = Modifier, onFullScreenEnter: () -> Unit = {}, onFullScreenExit: () -> Unit = {},) {
    Box(modifier = modifier.fillMaxSize()) {
        VideoPlayer(
            mediaItems = listOf(
                VideoPlayerMediaItem.StorageMediaItem(fileName),
            ),
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .background(MaterialTheme.colorScheme.onPrimaryContainer),
            onFullScreenEnter = onFullScreenEnter,
            onFullScreenExit = onFullScreenExit,
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
