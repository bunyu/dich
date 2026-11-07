package com.bunyu.dich.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.bunyu.dich.Prefs
import java.util.Locale

/**
 * Reads text aloud. Prefers Google's speech engine: Xiaomi phones may ship their own engine
 * as the default, and it often has no Vietnamese voice.
 */
class Speaker(context: Context, private val prefs: Prefs) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: Pair<String, Lang>? = null

    /** Package name of the engine in use, shown in the family settings. */
    var engineName: String = "?"
        private set

    init {
        start(GOOGLE_TTS)
    }

    private fun start(engine: String?) {
        var created: TextToSpeech? = null
        created = TextToSpeech(appContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts = created
                ready = true
                engineName = created?.defaultEngine.takeIf { engine == null } ?: engine ?: "?"
                pending?.let { (text, lang) -> speak(text, lang) }
                pending = null
            } else if (engine != null) {
                created?.shutdown()
                start(null) // Google engine missing: fall back to the phone's default engine.
            }
        }, engine)
    }

    fun speak(text: String, lang: Lang = Lang.VI) {
        val t = tts
        if (!ready || t == null) {
            pending = text to lang
            return
        }
        t.setSpeechRate(prefs.speechRate)
        val chosen = prefs.voiceName?.let { name -> t.voices?.firstOrNull { it.name == name } }
        if (lang == Lang.VI && chosen != null) {
            t.voice = chosen
        } else {
            t.language = lang.locale
        }
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dich")
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun isReady(): Boolean = ready

    /** Vietnamese voices installed on this phone, offline ones first. */
    fun vietnameseVoices(): List<Voice> =
        tts?.voices.orEmpty()
            .filter { it.locale.language == "vi" }
            .sortedWith(compareBy<Voice> { it.isNetworkConnectionRequired }.thenBy { it.name })

    fun hasVietnamese(): Boolean =
        (tts?.isLanguageAvailable(Locale("vi", "VN")) ?: TextToSpeech.LANG_NOT_SUPPORTED) >=
            TextToSpeech.LANG_AVAILABLE

    private companion object {
        const val GOOGLE_TTS = "com.google.android.tts"
    }
}
