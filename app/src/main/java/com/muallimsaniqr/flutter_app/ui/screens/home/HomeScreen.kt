package com.muallimsaniqr.flutter_app.ui.screens.home

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.domain.model.HeaderItem
import com.muallimsaniqr.flutter_app.domain.model.Language
import com.muallimsaniqr.flutter_app.domain.model.Lesson
import com.muallimsaniqr.flutter_app.ui.extensions.findAndroidActivity
import com.muallimsaniqr.flutter_app.ui.extensions.getDownloadedVideoFolder
import com.muallimsaniqr.flutter_app.ui.extensions.painter
import com.muallimsaniqr.flutter_app.ui.extensions.showToast
import com.muallimsaniqr.flutter_app.ui.extensions.strResource
import com.muallimsaniqr.flutter_app.ui.screens.scanner.ScannerActivity
import com.muallimsaniqr.flutter_app.ui.widgets.SelectLanguage
import com.muallimsaniqr.flutter_app.ui.widgets.buttons.MualimButton
import com.muallimsaniqr.flutter_app.ui.widgets.headers.Header
import com.muallimsaniqr.flutter_app.ui.widgets.toolbars.NavigatedToolbar
import com.muallimsaniqr.flutter_app.ui.widgets.video.VideoPlayer
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    vm: HomeVM = hiltViewModel(),
    lang: Language = Language.KYRGYZ,
    openOnBoarding: () -> Unit = {},
    onLanguageChanged: (Language) -> Unit = {}
) {

    val context = LocalContext.current

    var selectedFileName by remember { mutableIntStateOf(0) }
    var isOnFullScreen by remember { mutableStateOf(false) }

    val lazyColumnState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { activityResult ->
            val scanned = activityResult.data?.getStringExtra(ScannerActivity.RESULT_SUCCESS)
            if (scanned != null) {
                val fileName = vm.getFileNameExceptExtByScannerResult(scanned)
                if (fileName != 0) selectedFileName = fileName
            }
        }
    )

    // Video secildiginde: dersi "okundu" olarak kaydet ve listeyi o derse kaydir.
    // Onceki surumde bu islemler kompozisyonun icinde yapiliyordu (yan etki hatasi).
    LaunchedEffect(selectedFileName) {
        if (selectedFileName != 0) {
            vm.setScannedId(selectedFileName)
            val index = vm.getLessonIndexByFileName(selectedFileName)
            if (index >= 0) lazyColumnState.scrollToItem(index)
        }
    }

    val videoUri: Uri? = remember(selectedFileName) {
        if (selectedFileName == 0) {
            null
        } else {
            val file = context.getDownloadedVideoFolder().resolve("$selectedFileName.mp4")
            if (file.exists()) file.toUri() else null
        }
    }

    // Tam ekran modunda ekran yatay cevrilir ve sistem cubuklari gizlenir.
    val activity = remember { context.findAndroidActivity() }
    LaunchedEffect(isOnFullScreen) {
        activity?.requestedOrientation = if (isOnFullScreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    BackHandler(enabled = isOnFullScreen) { isOnFullScreen = false }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isOnFullScreen,
            drawerContent = {
                ModalDrawerSheet {
                    SelectLanguage(
                        language = lang,
                        onLanguageSelected = { selected ->
                            coroutineScope.launch { drawerState.close() }
                            onLanguageChanged(selected)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 20.dp)
                    )
                    MualimButton(
                        text = R.string.buy_book.strResource,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        coroutineScope.launch { drawerState.close() }
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
                            painter = R.drawable.ic_qr.painter,
                            contentDescription = "QR",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                topBar = {
                    NavigatedToolbar(
                        title = stringResource(R.string.app_name),
                        icon = R.drawable.ic_hamburger,
                        textAlignment = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        onNavigatedIconClick = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
            ) { paddings ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddings)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (videoUri != null) {
                            VideoPlayer(
                                uri = videoUri,
                                modifier = Modifier.fillMaxSize(),
                                isFullScreen = isOnFullScreen,
                                onFullScreenChanged = { isOnFullScreen = it }
                            )
                        } else {
                            EmptyVideoPlaceholder(onBuyBookClick = openOnBoarding)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = lazyColumnState
                    ) {
                        items(vm.items.size) { index ->
                            when (val item = vm.items[index]) {
                                is HeaderItem -> Header(
                                    text = item.title.strResource,
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                is Lesson -> {
                                    val isSelected = selectedFileName == item.fileNameExceptExt
                                    Text(
                                        text = item.title.strResource,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.background
                                            )
                                            .clickable {
                                                if (vm.checkBeforeScanned(item.id)) {
                                                    selectedFileName = item.fileNameExceptExt
                                                } else {
                                                    context.showToast(R.string.open_lesson_hint)
                                                }
                                            }
                                            .padding(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tam ekran: video butun ekrani kaplar.
        if (isOnFullScreen && videoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                VideoPlayer(
                    uri = videoUri,
                    modifier = Modifier.fillMaxSize(),
                    isFullScreen = true,
                    onFullScreenChanged = { isOnFullScreen = it }
                )
            }
        }
    }
}

@Composable
private fun EmptyVideoPlaceholder(onBuyBookClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7F))
                .clickable { onBuyBookClick() },
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
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = R.drawable.ic_qr.painter,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "QR"
            )
            Text(
                text = stringResource(R.string.tap_to_open_video),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}
