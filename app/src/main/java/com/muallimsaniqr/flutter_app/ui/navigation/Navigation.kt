package com.muallimsaniqr.flutter_app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.muallimsaniqr.flutter_app.ui.screens.downloading.FileDownloaderVM
import com.muallimsaniqr.flutter_app.ui.screens.downloading.FileDownloadingScreen
import com.muallimsaniqr.flutter_app.ui.screens.home.HomeScreen
import com.muallimsaniqr.flutter_app.ui.screens.home.HomeVM
import com.muallimsaniqr.flutter_app.ui.screens.onboarding.OnboardingScreen
import com.muallimsaniqr.flutter_app.utils.language.LocaleUtils

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    finishActivity: () -> Unit = {},
) {

    NavHost(navController = navController, startDestination = Screens.Home.route) {

        composable(Screens.Onbaording.route) {
            BackHandler {
                finishActivity()
            }
            OnboardingScreen {
                navController.navigate(Screens.Home.route) {
                    launchSingleTop = true
                }
            }
        }

        composable(Screens.Home.route) {
            BackHandler {
                finishActivity()
            }
            val vm: HomeVM = hiltViewModel()
            val lang = remember {
                mutableStateOf(vm.getLanguage())
            }

            when {
                !vm.isOnBoardingShown -> {
                    navController.navigate(Screens.Onbaording.route)
                }

                !vm.areVideosDownloaded -> {
                    navController.navigate(Screens.Downloading.route) {
                        launchSingleTop = true
                    }
                }

                else -> {
                    HomeScreen(
                        vm = vm,
                        lang = lang.value,
                        openOnBoarding = {
                            navController.navigate(Screens.Onbaording.route) {
                                launchSingleTop = true
                            }
                        },
                        onLanguageChanged = {
                            lang.value = it
                            vm.setLanguage(it)
                        }
                    )
                }
            }
        }

        composable(Screens.Downloading.route) {
            val vm: FileDownloaderVM = hiltViewModel()

            FileDownloadingScreen(vm) {
                navController.navigate(Screens.Home.route) {
                    launchSingleTop = true
                }
            }

        }


    }

}