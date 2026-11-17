package com.bunyu.dich.engine

import androidx.annotation.StringRes
import com.bunyu.dich.R
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

/** The languages the app works with. Vietnamese is hers; every other one is "the other person's". */
enum class Lang(
    val code: String,
    /** Locale for reading aloud. */
    val locale: Locale,
    /** BCP 47 tag for speech recognition. */
    val speechTag: String,
    @StringRes val label: Int,
    /** The language's name in itself, shown to the other person so they can pick their own. */
    val nativeName: String,
    /** How the language is described to Gemini. */
    val geminiName: String,
) {
    VI(TranslateLanguage.VIETNAMESE, Locale("vi", "VN"), "vi-VN", R.string.lang_vi, "Tiếng Việt", "Vietnamese"),
    FR(TranslateLanguage.FRENCH, Locale.FRANCE, "fr-FR", R.string.lang_fr, "Français", "French"),
    EN(TranslateLanguage.ENGLISH, Locale.UK, "en-GB", R.string.lang_en, "English", "English"),

    /**
     * Morocco: speech is recognised as Moroccan Arabic (Darija); written output is simple Modern
     * Standard Arabic, which every Moroccan reads and which the Arabic voice reads correctly.
     * (French, also widely used in Morocco, is [FR].)
     */
    AR(
        TranslateLanguage.ARABIC, Locale("ar"), "ar-MA", R.string.lang_ar, "العربية",
        "Arabic as used in Morocco (input may be Moroccan Darija; write simple Modern Standard Arabic that Moroccans understand)",
    ),
    JA(TranslateLanguage.JAPANESE, Locale.JAPAN, "ja-JP", R.string.lang_ja, "日本語", "Japanese (polite form)"),
    ZH(TranslateLanguage.CHINESE, Locale.SIMPLIFIED_CHINESE, "zh-CN", R.string.lang_zh, "中文", "Simplified Chinese (Mandarin)"),
    KO(TranslateLanguage.KOREAN, Locale.KOREA, "ko-KR", R.string.lang_ko, "한국어", "Korean (polite form)"),
    ;

    companion object {
        /** Languages the other person can speak, in the order they are offered. */
        val others: List<Lang> = entries.filter { it != VI }

        fun fromCode(code: String?): Lang? = entries.firstOrNull { it.code == code }
    }
}
