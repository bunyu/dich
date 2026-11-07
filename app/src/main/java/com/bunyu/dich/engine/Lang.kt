package com.bunyu.dich.engine

import androidx.annotation.StringRes
import com.bunyu.dich.R
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

/** The three languages the app works with. */
enum class Lang(val code: String, val locale: Locale, @StringRes val label: Int) {
    VI(TranslateLanguage.VIETNAMESE, Locale("vi", "VN"), R.string.lang_vi),
    FR(TranslateLanguage.FRENCH, Locale.FRANCE, R.string.lang_fr),
    EN(TranslateLanguage.ENGLISH, Locale.UK, R.string.lang_en),
}
