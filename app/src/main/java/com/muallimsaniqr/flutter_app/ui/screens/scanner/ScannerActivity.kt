package com.muallimsaniqr.flutter_app.ui.screens.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.ui.extensions.openAppSettings
import com.muallimsaniqr.flutter_app.ui.extensions.strResource
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme
import com.muallimsaniqr.flutter_app.ui.widgets.buttons.MualimButton
import com.muallimsaniqr.flutter_app.utils.scanner.AnalyzerType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MualimSaniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScannerContent { result ->
                        setResult(Activity.RESULT_OK, Intent().apply {
                            putExtra(RESULT_SUCCESS, result)
                        })
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        const val RESULT_SUCCESS = "SCANNER_RESULT_SUCCESS"
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun ScannerContent(result: (String?) -> Unit = {}) {

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
        var analyzerType by remember { mutableStateOf(AnalyzerType.BARCODE) }

        if (cameraPermissionState.status.isGranted) {
            CameraScreen(analyzerType, result)
        } else {

            MualimButton(text = R.string.camera_permission_needed.strResource,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                onClick = {
                    context.openAppSettings()
                }
            )

            if (cameraPermissionState.status.shouldShowRationale) { }
            else {
                SideEffect {
                    cameraPermissionState.run { launchPermissionRequest() }
                }
            }
        }
    }
}