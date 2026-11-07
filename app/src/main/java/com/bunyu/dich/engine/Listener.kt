package com.bunyu.dich.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Turns speech into text with Android's speech recognizer. Tries offline first; if the
 * offline language pack is missing, retries once online. Must be used from the main thread.
 *
 * One recognizer is reused for every press: creating and destroying one each time makes Google's
 * recognizer answer "busy" or "client error", which forced several presses before it listened.
 * Every start gets a session number, and callbacks from an older session are ignored, so a late
 * error from a finished recognition can no longer replace a good result with "Có lỗi".
 */
class Listener(context: Context) {
    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var session = 0

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun start(
        lang: Lang,
        onPartial: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (ListenError) -> Unit,
    ) = start(lang, preferOffline = true, busyRetries = 2, onPartial, onResult, onError)

    private fun recognizer(): SpeechRecognizer =
        recognizer ?: SpeechRecognizer.createSpeechRecognizer(appContext).also { recognizer = it }

    private fun start(
        lang: Lang,
        preferOffline: Boolean,
        busyRetries: Int,
        onPartial: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (ListenError) -> Unit,
    ) {
        main.removeCallbacksAndMessages(null)
        val my = ++session
        val r = recognizer()
        r.cancel()
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                if (my != session) return
                session++ // this recognition is finished: ignore anything it sends later
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (text.isNullOrBlank()) onError(ListenError.NotHeard) else onResult(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                if (my != session) return
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let(onPartial)
            }

            override fun onError(error: Int) {
                if (my != session) return
                // Busy / client errors right after a previous recognition: wait a moment and retry.
                if ((error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) &&
                    busyRetries > 0
                ) {
                    session++
                    if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        recognizer?.destroy()
                        recognizer = null
                    }
                    main.postDelayed({
                        start(lang, preferOffline, busyRetries - 1, onPartial, onResult, onError)
                    }, 400)
                    return
                }
                val offlineMissing = error == ERROR_LANGUAGE_NOT_SUPPORTED ||
                    error == ERROR_LANGUAGE_UNAVAILABLE ||
                    error == SpeechRecognizer.ERROR_SERVER
                if (preferOffline && offlineMissing) {
                    start(lang, preferOffline = false, busyRetries, onPartial, onResult, onError)
                    return
                }
                session++
                onError(
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> ListenError.NotHeard
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                        SpeechRecognizer.ERROR_SERVER,
                        ERROR_LANGUAGE_NOT_SUPPORTED,
                        ERROR_LANGUAGE_UNAVAILABLE -> ListenError.NeedsInternet
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> ListenError.NoPermission
                        else -> ListenError.Other
                    }
                )
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.speechTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang.speechTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
            // Older people pause while speaking: wait a little longer before stopping.
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        }
        r.startListening(intent)
    }

    /** Stop recording and deliver what was heard so far. */
    fun finish() {
        recognizer?.stopListening()
    }

    /** Stop and discard; later callbacks are ignored. */
    fun cancel() {
        main.removeCallbacksAndMessages(null)
        session++
        recognizer?.cancel()
    }

    /** Free the recognizer (when leaving the conversation screen). */
    fun release() {
        cancel()
        recognizer?.destroy()
        recognizer = null
    }

    private companion object {
        // SpeechRecognizer constants added in Android 12; plain values so older phones compile too.
        const val ERROR_LANGUAGE_NOT_SUPPORTED = 12
        const val ERROR_LANGUAGE_UNAVAILABLE = 13
    }
}

enum class ListenError { NotHeard, NeedsInternet, NoPermission, Other }
