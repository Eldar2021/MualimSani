package com.muallimsaniqr.flutter_app.utils.language

import android.content.Context
import android.content.res.Configuration
import com.muallimsaniqr.flutter_app.domain.model.Language
import java.util.Locale

object LocaleUtils {

    /**
     * Verilen [language] icin yapilandirilmis bir [Context] dondurur.
     *
     * Activity'lerin `attachBaseContext` metodunda kullanilir. Onceki surumde
     * kullanilan `Resources.updateConfiguration` API'si Android 25'ten beri
     * kullanimdan kaldirilmisti ve Android 13+ cihazlarda guvenilir calismiyordu.
     */
    fun wrap(context: Context, language: Language): Context {
        val locale = Locale.forLanguageTag(language.lang)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(configuration)
    }
}
