package com.muallimsaniqr.flutter_app.ui.navigation

sealed class Screens(val route: String) {

    object Home : Screens("home")
    object Onbaording: Screens("onboarding")
    object Downloading: Screens("downloading")
}