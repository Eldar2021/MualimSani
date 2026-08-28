package com.muallimsaniqr.flutter_app.data.repository

import com.muallimsaniqr.flutter_app.common.base.BaseRepo
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.domain.repository.MualimRepository
import javax.inject.Inject

class MualimRepoImpl @Inject constructor(
    private val storage: AppPreference
): BaseRepo(), MualimRepository {
    override fun isOnboardingShown() = storage.isOnboardingShown()

    override fun setOnboardingShown(isShown: Boolean) {
        storage.setOnboardingShown(isShown)
    }

    override fun setScannedId(id: Int) {
        storage.setScannedId(id)
    }

    override fun getScannedIds(): List<Int> {
        return storage.getScannedIds().toList()
    }

    override fun getAreVideosDownloaded(): Boolean {
        return storage.areFilesDownloaded()
    }

    override fun setAreVideosDownloaded(isDownloaded: Boolean) {
        storage.setFilesDownloaded(isDownloaded)
    }
}