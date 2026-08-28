package com.muallimsaniqr.flutter_app.utils.language

import android.content.Context
import android.content.res.Configuration
import com.muallimsaniqr.flutter_app.domain.model.Language
import java.util.Locale

object LocaleUtils {
fun setLocale(c: Context, language: Language) = updateResources(c, language.lang)

private fun updateResources(context: Context, language: String) {
    context.resources.apply {
        val locale = Locale(language)
        val config = Configuration(configuration)

        context.createConfigurationContext(configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        // deprecated
        context.resources.updateConfiguration(config, displayMetrics)
        //context.createConfigurationContext(config)
    }
  }
}