package com.muallimsaniqr.flutter_app.ui.navigation

sealed class Screens(val route: String) {

    data object Home : Screens("home")
    data object Onboarding : Screens("onboarding")
    data object Downloading : Screens("downloading")
}
