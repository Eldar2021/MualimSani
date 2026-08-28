package com.muallimsaniqr.flutter_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.ui.navigation.AppNavHost
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme
import com.muallimsaniqr.flutter_app.utils.language.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var localStorage: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            LocaleUtils.setLocale(this, localStorage.getLanguage())
            val navController = rememberNavController()

            // uncomment to enable video caching for offline playback if you watch videos via online streaming
            // in our case, we are downloading videos and playing them offline
            // VideoPlayerCacheManager.initialize(this, 1024 * 1024 * 1024) // 1GB

            MualimSaniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    AppNavHost(
                        navController = navController,
                        finishActivity = { finish() },
                    )
                }
            }
        }
    }
}
