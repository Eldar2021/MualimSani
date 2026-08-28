package com.muallimsaniqr.flutter_app.domain.repository

interface MualimRepository {

    fun isOnboardingShown(): Boolean

    fun setOnboardingShown(isShown: Boolean)

    fun setScannedId(id: Int)

    fun getScannedIds(): List<Int>

    fun getAreVideosDownloaded(): Boolean

    fun setAreVideosDownloaded(isDownloaded: Boolean)

}