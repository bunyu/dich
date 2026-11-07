package com.bunyu.dich.engine

import androidx.annotation.StringRes
import com.bunyu.dich.R
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

/** The three languages the app works with. */
enum class Lang(
    val code: String,
    val locale: Locale,
    /** BCP 47 tag for speech recognition. */
    val speechTag: String,
    @StringRes val label: Int,
) {
    VI(TranslateLanguage.VIETNAMESE, Locale("vi", "VN"), "vi-VN", R.string.lang_vi),
    FR(TranslateLanguage.FRENCH, Locale.FRANCE, "fr-FR", R.string.lang_fr),
    EN(TranslateLanguage.ENGLISH, Locale.UK, "en-GB", R.string.lang_en),
}
