package com.bunyu.dich

import android.content.Context

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

    private companion object {
        const val KEY_VOICE = "voice"
        const val KEY_TEXT_SIZE = "text_size"
        const val KEY_SPEECH_RATE = "speech_rate"
    }
}
