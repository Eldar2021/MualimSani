package com.muallimsaniqr.flutter_app.ui.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import com.muallimsaniqr.flutter_app.utils.constants.CommonConstants
import java.io.File

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes id: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(id), duration)
}

fun Context.findAndroidActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
fun Context.openAppSettings() {
    val activity = this.findAndroidActivity() ?: return
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", activity.packageName , null)
    )
    startActivity(intent)
}

fun Context.openWhatsappByPhoneNumber(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/$phoneNumber")
    }
    startActivity(intent)
}

fun Context.openWhatsappByLink(link: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(link)
    }
    startActivity(intent)
}

fun Context.openTelegramByUserName(userName: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://t.me/$userName")
    }
    startActivity(intent)
}

fun Context.openInstagramByUserName(userName: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://www.instagram.com/$userName")
    }
    startActivity(intent)
}


fun Context.getDownloadedVideoFolder(): File {
    val internalStorage = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val videoFolder = File(internalStorage, CommonConstants.VIDEOS_DIRECTION)
    videoFolder.mkdirs()
    return videoFolder
}