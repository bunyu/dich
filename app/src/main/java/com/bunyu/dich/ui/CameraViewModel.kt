package com.bunyu.dich.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bunyu.dich.R
import com.bunyu.dich.dichApp
import com.bunyu.dich.engine.Engine
import com.bunyu.dich.engine.Lang
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** One translated block: what was printed and what it means in Vietnamese. */
data class TranslatedBlock(val original: String, val vietnamese: String)

sealed interface CameraState {
    data object Aiming : CameraState
    data class Working(val photo: Bitmap, @StringRes val message: Int) : CameraState
    data class Done(
        val photo: Bitmap,
        val from: Lang,
        val blocks: List<TranslatedBlock>,
        val engine: Engine,
    ) : CameraState {
        val vietnamese: String get() = blocks.joinToString("\n\n") { it.vietnamese }
        val original: String get() = blocks.joinToString("\n\n") { it.original }
    }
    data class Failed(@StringRes val message: Int) : CameraState
}

/** Photo -> text -> Vietnamese -> read aloud. Every step runs on the phone. */
class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application.dichApp
    private var job: Job? = null

    var state by mutableStateOf<CameraState>(CameraState.Aiming)
        private set

    fun onPhoto(photo: Bitmap) {
        job?.cancel()
        job = viewModelScope.launch {
            state = CameraState.Working(photo, R.string.reading)
            val lines = try {
                app.textReader.read(photo)
            } catch (e: Exception) {
                fail(R.string.error_generic)
                return@launch
            }
            if (lines.isEmpty()) {
                fail(R.string.no_text)
                return@launch
            }

            val from = runCatching { app.languageGuesser.guess(lines.joinToString(" ")) }
                .getOrDefault(Lang.FR)
            val online = app.translator.willUseOnline()
            val haveModels = online || runCatching { app.offlineTranslator.downloadedLanguages() }
                .getOrDefault(emptySet())
                .containsAll(setOf(from, Lang.VI))
            state = CameraState.Working(
                photo,
                if (haveModels) R.string.translating else R.string.downloading_models,
            )

            val blocks = try {
                val translated = app.translator.translateAll(lines, from, Lang.VI)
                lines.zip(translated) { original, vi -> TranslatedBlock(original, vi) }
            } catch (e: Exception) {
                fail(if (haveModels) R.string.error_generic else R.string.error_need_internet)
                return@launch
            }
            val done = CameraState.Done(photo, from, blocks, app.translator.lastEngine)
            state = done
            app.history.add(done.vietnamese, done.original, from)
            if (app.prefs.autoRead) readAloud()
        }
    }

    fun onCaptureError() = fail(R.string.capture_error)

    /** Reads the translation block by block, starting at [from]. */
    fun readAloud(from: Int = 0) {
        (state as? CameraState.Done)?.let { done ->
            app.speaker.play(done.blocks.map { it.vietnamese }, Lang.VI, from)
        }
    }

    fun pause() = app.speaker.pause()
    fun resume() = app.speaker.resume()
    fun replay() = readAloud(0)
    fun stopReading() = app.speaker.stop()

    fun reset() {
        job?.cancel()
        app.speaker.stop()
        state = CameraState.Aiming
    }

    private fun fail(@StringRes message: Int) {
        state = CameraState.Failed(message)
        if (app.prefs.autoRead) app.speaker.speak(getApplication<Application>().getString(message))
    }

    override fun onCleared() {
        app.speaker.stop()
    }
}
