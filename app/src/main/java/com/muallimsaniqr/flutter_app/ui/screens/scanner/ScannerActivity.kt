package com.muallimsaniqr.flutter_app.ui.screens.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.ui.extensions.openAppSettings
import com.muallimsaniqr.flutter_app.ui.extensions.strResource
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme
import com.muallimsaniqr.flutter_app.ui.widgets.buttons.MualimButton
import com.muallimsaniqr.flutter_app.utils.language.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScannerActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = AppPreference(newBase).getLanguage()
        super.attachBaseContext(LocaleUtils.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MualimSaniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScannerContent { result ->
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(RESULT_SUCCESS, result)
                        )
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

/**
 * Kamera iznini ister ve izin verildiginde QR tarayiciyi gosterir.
 *
 * Onceki surumde bunun icin bakimi birakilan Accompanist Permissions kutuphanesi
 * kullaniliyordu; artik Android'in yerlesik izin sozlesmesi kullaniliyor.
 */
@Composable
fun ScannerContent(onResult: (String) -> Unit = {}) {

    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            permissionRequested = true
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (hasCameraPermission) {
            CameraScreen(onBarcodeScanned = onResult)
        } else if (permissionRequested) {
            // Izin reddedildi: kullaniciyi uygulama ayarlarina yonlendir.
            MualimButton(
                text = R.string.camera_permission_needed.strResource,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                onClick = { context.openAppSettings() }
            )
        }
    }
}
