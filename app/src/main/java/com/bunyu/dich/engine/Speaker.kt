package com.bunyu.dich.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bunyu.dich.Prefs
import java.util.Locale

enum class PlayState { Idle, Playing, Paused }

/**
 * Reads text aloud, one segment (paragraph or sentence) at a time, so reading can be paused,
 * resumed from the same segment, restarted or stopped. [playState] and [segmentIndex] are
 * Compose state: screens can show the right button and highlight the part being read.
 *
 * Prefers Google's speech engine: Xiaomi phones may ship their own engine as the default, and it
 * often has no Vietnamese voice.
 */
class Speaker(context: Context, private val prefs: Prefs) {
    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var ready = false

    private var segments: List<String> = emptyList()
    private var segmentLang: Lang = Lang.VI
    private var pendingStart: Int? = null

    /** Bumped on every new play/stop so callbacks from an earlier reading are ignored. */
    private var generation = 0

    var playState by mutableStateOf(PlayState.Idle)
        private set

    /** Index of the segment being read (or paused at); -1 when idle. */
    var segmentIndex by mutableIntStateOf(-1)
        private set

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
                val t = created ?: return@TextToSpeech
                t.setOnUtteranceProgressListener(progressListener)
                tts = t
                ready = true
                engineName = t.defaultEngine.takeIf { engine == null } ?: engine ?: "?"
                pendingStart?.let { speakFrom(it) }
                pendingStart = null
            } else if (engine != null) {
                created?.shutdown()
                start(null) // Google engine missing: fall back to the phone's default engine.
            }
        }, engine)
    }

    /** Reads [text] split into sentences. */
    fun speak(text: String, lang: Lang = Lang.VI) = play(splitSentences(text), lang)

    /** Reads [parts] in order, starting at [from]. */
    fun play(parts: List<String>, lang: Lang = Lang.VI, from: Int = 0) {
        segments = parts.map { it.trim() }.filter { it.isNotEmpty() }
        segmentLang = lang
        if (segments.isEmpty()) {
            stop()
            return
        }
        speakFrom(from.coerceIn(0, segments.lastIndex))
    }

    /** Pauses; [resume] continues from the start of the segment that was being read. */
    fun pause() {
        if (playState != PlayState.Playing) return
        generation++
        tts?.stop()
        playState = PlayState.Paused
    }

    fun resume() {
        if (playState == PlayState.Paused) speakFrom(segmentIndex.coerceAtLeast(0))
    }

    /** Reads everything again from the beginning. */
    fun replay() {
        if (segments.isNotEmpty()) speakFrom(0)
    }

    fun stop() {
        generation++
        pendingStart = null
        tts?.stop()
        playState = PlayState.Idle
        segmentIndex = -1
    }

    private fun speakFrom(index: Int) {
        val t = tts
        generation++
        playState = PlayState.Playing
        segmentIndex = index
        if (!ready || t == null) {
            pendingStart = index
            return
        }
        t.setSpeechRate(prefs.speechRate)
        val chosen = prefs.voiceName?.let { name -> t.voices?.firstOrNull { it.name == name } }
        if (segmentLang == Lang.VI && chosen != null) {
            t.voice = chosen
        } else {
            t.language = segmentLang.locale
        }
        val gen = generation
        for (k in index until segments.size) {
            val mode = if (k == index) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            t.speak(segments[k], mode, null, "seg-$gen-$k")
        }
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = onMain(utteranceId) { k ->
            segmentIndex = k
        }

        override fun onDone(utteranceId: String?) = onMain(utteranceId) { k ->
            if (k >= segments.lastIndex) {
                playState = PlayState.Idle
                segmentIndex = -1
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = onMain(utteranceId) {
            playState = PlayState.Idle
            segmentIndex = -1
        }

        /** Runs [block] on the main thread if the id belongs to the current reading. */
        private fun onMain(utteranceId: String?, block: (Int) -> Unit) {
            val parts = utteranceId?.split('-') ?: return
            if (parts.size != 3 || parts[0] != "seg") return
            val gen = parts[1].toIntOrNull() ?: return
            val k = parts[2].toIntOrNull() ?: return
            main.post { if (gen == generation) block(k) }
        }
    }

    fun isSpeaking(): Boolean = playState == PlayState.Playing

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

        fun splitSentences(text: String): List<String> =
            text.split(Regex("""(?<=[.!?…])\s+|\n+"""))
    }
}
