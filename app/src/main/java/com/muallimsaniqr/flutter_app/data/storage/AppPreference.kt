package com.muallimsaniqr.flutter_app.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.muallimsaniqr.flutter_app.domain.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Singleton
class AppPreference(@ApplicationContext context: Context) {

    private var sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("MUALIM_APP_PREF", Context.MODE_PRIVATE)
    }

    private fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    private fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun setInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    private fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }





    fun isOnboardingShown(): Boolean {
        return getBoolean(PREF_IS_ONBOARDING_SHOWN, false)
    }

    fun setOnboardingShown(isShown: Boolean) {
        setBoolean(PREF_IS_ONBOARDING_SHOWN, isShown)
    }

    fun setScannedId(id: Int) {
        val scannedIds = getScannedIds().toMutableSet()
        if (id in scannedIds) return
        scannedIds.add(id)
        val joinedIds = scannedIds.joinToString(separator = ",") { it.toString() }
        setString(PREF_SCANNED_ID, joinedIds)
    }

    fun getScannedIds(): Set<Int> {
        val ids = getString(PREF_SCANNED_ID, "")
        if (ids.isBlank()) return emptySet()
        val scannedId = ids.split(",").map { it.toIntOrNull() ?: 0 }.toMutableSet()
        scannedId.remove(0)
        return scannedId
    }

    fun setAlreadyDownloadedVideo(videoName: String) {
        val items = getAlreadyDownloadedVideos().toMutableSet()
        if (videoName in items) return
        items.add(videoName)
        val joinedVideosName = items.joinToString(separator = ",")
        setString(PREF_ALREADY_DOWNLOADED_ITEMS, joinedVideosName)
    }

    fun getAlreadyDownloadedVideos(): Set<String> {
        val ids = getString(PREF_ALREADY_DOWNLOADED_ITEMS, "")
        if (ids.isBlank()) return emptySet()
        return ids.split(",").toMutableSet()
    }

    fun areFilesDownloaded(): Boolean {
        return getBoolean(PREF_ARE_FILES_DOWNLOADED, false)
    }

    fun setFilesDownloaded(isDownloaded: Boolean) {
        setBoolean(PREF_ARE_FILES_DOWNLOADED, isDownloaded)
    }

    fun setLanguage(language: Language) {
        setInt(PREF_LANGUAGE, language.ordinal)
    }

    fun getLanguage(): Language {
        return Language.values()[getInt(PREF_LANGUAGE, Language.KYRGYZ.ordinal)]
    }

    fun clearData() {
        sharedPreferences.edit().clear().apply()
    }


    companion object {
        private const val PREF_IS_ONBOARDING_SHOWN = "IS_ONBOARDING_SHOWN"
        private const val PREF_SCANNED_ID = "SCANNED_ID"
        private const val PREF_ALREADY_DOWNLOADED_ITEMS = "ALREADY_DOWNLOADED_ITEMS"
        private const val PREF_ARE_FILES_DOWNLOADED = "ARE_FILES_DOWNLOADED"
        private const val PREF_LANGUAGE = "LANGUAGE"
    }

}