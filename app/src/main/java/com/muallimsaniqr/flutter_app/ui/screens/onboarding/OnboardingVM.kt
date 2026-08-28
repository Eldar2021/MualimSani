package com.muallimsaniqr.flutter_app.ui.screens.onboarding

import com.muallimsaniqr.flutter_app.common.base.BaseVM
import com.muallimsaniqr.flutter_app.domain.repository.MualimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingVM @Inject constructor(
    private val repo: MualimRepository
): BaseVM() {

    fun setOnboardingShown(isShown: Boolean) {
        repo.setOnboardingShown(isShown)
    }

}