package com.muallimsaniqr.flutter_app.ui.screens.scanner

import com.muallimsaniqr.flutter_app.common.base.BaseVM
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScannerVM @Inject constructor(
    private val appPref: AppPreference
): BaseVM()  {

    var scannerResult = ""

}