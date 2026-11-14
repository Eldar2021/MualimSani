package com.muallimsaniqr.flutter_app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.ui.navigation.AppNavHost
import com.muallimsaniqr.flutter_app.ui.navigation.Screens
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme
import com.muallimsaniqr.flutter_app.utils.language.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var localStorage: AppPreference

    override fun attachBaseContext(newBase: Context) {
        // Hilt henuz hazir olmadigi icin tercih dogrudan okunur.
        val language = AppPreference(newBase).getLanguage()
        super.attachBaseContext(LocaleUtils.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Acilista hangi ekranin gosterilecegi bir kez burada belirlenir.
        // Toplu indirme ekrani artik acilista gosterilmez; videolar ders acildikca iner.
        val startDestination =
            if (localStorage.isOnboardingShown()) Screens.Home.route else Screens.Onboarding.route

        setContent {
            MualimSaniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        navController = rememberNavController(),
                        startDestination = startDestination,
                        currentLanguage = localStorage.getLanguage(),
                        onLanguageChanged = { language ->
                            localStorage.setLanguage(language)
                            // Yeni dilin butun ekranlara uygulanmasi icin activity yeniden olusturulur.
                            recreate()
                        }
                    )
                }
            }
        }
    }
}
