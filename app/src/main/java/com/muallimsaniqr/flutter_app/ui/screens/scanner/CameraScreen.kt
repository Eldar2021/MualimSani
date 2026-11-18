package com.muallimsaniqr.flutter_app.ui.screens.scanner

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.utils.scanner.BarcodeAnalyzer
import java.util.concurrent.Executors

@Composable
fun CameraScreen(onBarcodeScanned: (String) -> Unit = {}) {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Goruntu analizi ana is parcacigini mesgul etmesin diye ayri bir executor kullanilir.
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(localContext) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val previewView = PreviewView(context)
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }
                val selector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(analysisExecutor, BarcodeAnalyzer { value ->
                            ContextCompat.getMainExecutor(context).execute { onBarcodeScanned(value) }
                        })
                    }

                cameraProviderFuture.addListener({
                    runCatching {
                        cameraProviderFuture.get().apply {
                            unbindAll()
                            bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
                        }
                    }.onFailure {
                        Log.e("CameraScreen", "Kamera baglanamadi", it)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            }
        )

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.lottie_scanner)
        )
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .align(Alignment.Center)
                .size(350.dp)
        )
    }
}
