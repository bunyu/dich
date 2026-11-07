package com.bunyu.dich

import android.content.Context
import com.bunyu.dich.engine.Lang

/** Small settings store. Changed only from the hidden family settings screen. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("dich", Context.MODE_PRIVATE)

    /** Name of the chosen Vietnamese TTS voice, or null for the engine default. */
    var voiceName: String?
        get() = sp.getString(KEY_VOICE, null)
        set(value) = sp.edit().putString(KEY_VOICE, value).apply()

    /** Size of translated text, in sp. */
    var textSizeSp: Float
        get() = sp.getFloat(KEY_TEXT_SIZE, 30f)
        set(value) = sp.edit().putFloat(KEY_TEXT_SIZE, value).apply()

    /** 1.0 is normal speed; a little slower is easier to follow. */
    var speechRate: Float
        get() = sp.getFloat(KEY_SPEECH_RATE, 0.85f)
        set(value) = sp.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    /** Read results aloud automatically. When off, text is only spoken when a button is pressed. */
    var autoRead: Boolean
        get() = sp.getBoolean(KEY_AUTO_READ, true)
        set(value) = sp.edit().putBoolean(KEY_AUTO_READ, value).apply()

    /** Language of the other person in conversation mode (French or English). */
    var vendorLang: Lang
        get() = if (sp.getString(KEY_VENDOR_LANG, "fr") == "en") Lang.EN else Lang.FR
        set(value) = sp.edit().putString(KEY_VENDOR_LANG, if (value == Lang.EN) "en" else "fr").apply()

    /**
     * Google Cloud Translation API key typed in family settings, or else the one built into the APK
     * from local.properties. Empty means offline translation only.
     */
    var googleApiKey: String?
        get() = sp.getString(KEY_GOOGLE_API_KEY, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.GOOGLE_API_KEY.takeIf { it.isNotBlank() }
        set(value) = sp.edit().putString(KEY_GOOGLE_API_KEY, value?.trim()).apply()

    /** Gemini API key (Google AI Studio) typed in family settings, or else the one built into the APK. */
    var geminiApiKey: String?
        get() = sp.getString(KEY_GEMINI_API_KEY, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() }
        set(value) = sp.edit().putString(KEY_GEMINI_API_KEY, value?.trim()).apply()

    /** Only the key typed on the phone (not the built-in one), for the settings field. */
    val typedGeminiKey: String get() = sp.getString(KEY_GEMINI_API_KEY, null).orEmpty()
    val typedGoogleKey: String get() = sp.getString(KEY_GOOGLE_API_KEY, null).orEmpty()

    private companion object {
        const val KEY_GEMINI_API_KEY = "gemini_api_key"
        const val KEY_GOOGLE_API_KEY = "google_api_key"
        const val KEY_AUTO_READ = "auto_read"
        const val KEY_VENDOR_LANG = "vendor_lang"
        const val KEY_VOICE = "voice"
        const val KEY_TEXT_SIZE = "text_size"
        const val KEY_SPEECH_RATE = "speech_rate"
    }
}
