package com.muallimsaniqr.flutter_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.muallimsaniqr.flutter_app.domain.model.Language
import com.muallimsaniqr.flutter_app.ui.screens.downloading.FileDownloaderVM
import com.muallimsaniqr.flutter_app.ui.screens.downloading.FileDownloadingScreen
import com.muallimsaniqr.flutter_app.ui.screens.home.HomeScreen
import com.muallimsaniqr.flutter_app.ui.screens.onboarding.OnboardingScreen

/**
 * Uygulamanin gezinme grafigi.
 *
 * Onceki surumde acilis ekrani her zaman [Screens.Home] idi ve onboarding/indirme
 * kararlari `composable { }` blogunun icinde, kompozisyon sirasinda `navigate()`
 * cagrilarak veriliyordu. Bu Compose'da hatali bir kullanimdir (ekran iki kez
 * cizilir, geri yiginina yanlis kayitlar birikir). Artik baslangic ekrani
 * [startDestination] ile disaridan belirleniyor ve gecisler geri yiginini
 * dogru sekilde temizliyor.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    onLanguageChanged: (Language) -> Unit = {},
    currentLanguage: Language = Language.KYRGYZ,
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(Screens.Onboarding.route) {
            OnboardingScreen {
                // Ana ekrandan "kitap satin al" ile acildiysa geri don;
                // ilk acilis akisiysa siradaki ekrana gec.
                val cameFromAnotherScreen = navController.previousBackStackEntry != null
                if (cameFromAnotherScreen) {
                    navController.popBackStack()
                } else {
                    // Videolar artik toplu indirilmiyor; ders acildikca tek tek iner.
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Onboarding.route) { inclusive = true }
                    }
                }
            }
        }

        // Yan menudeki "tum dersleri indir" (internetsiz kullanim icin). Bitince ana ekrana doner.
        composable(Screens.Downloading.route) {
            val vm: FileDownloaderVM = hiltViewModel()

            FileDownloadingScreen(vm) {
                navController.popBackStack()
            }
        }

        composable(Screens.Home.route) {
            HomeScreen(
                lang = currentLanguage,
                openOnBoarding = {
                    navController.navigate(Screens.Onboarding.route) {
                        launchSingleTop = true
                    }
                },
                openDownloadAll = {
                    navController.navigate(Screens.Downloading.route) {
                        launchSingleTop = true
                    }
                },
                onLanguageChanged = onLanguageChanged
            )
        }
    }
}
