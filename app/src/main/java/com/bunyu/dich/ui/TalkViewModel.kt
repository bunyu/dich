package com.bunyu.dich.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bunyu.dich.dichApp
import com.bunyu.dich.engine.Engine
import com.bunyu.dich.engine.Lang
import com.bunyu.dich.engine.ListenError
import kotlinx.coroutines.launch

enum class Side { Her, Vendor }

/** One turn of the conversation: who spoke, what was heard, and its translation. */
data class Turn(
    val speaker: Side,
    val heard: String,
    val translated: String,
    /** Engine that translated it; null for ready-made phrases. */
    val engine: Engine? = null,
) {
    /** The Vietnamese side of this turn. */
    val vietnamese: String get() = if (speaker == Side.Her) heard else translated

    /** The French or English side of this turn. */
    val foreign: String get() = if (speaker == Side.Her) translated else heard
}

/** A short status line shown on one half of the screen, in that half's language. */
sealed interface Status {
    data object Listening : Status
    data object Translating : Status
    data class Failed(val error: ListenError) : Status
}

data class TalkUi(
    val vendorLang: Lang,
    val listening: Side? = null,
    val partial: String = "",
    val herStatus: Status? = null,
    val vendorStatus: Status? = null,
    val last: Turn? = null,
)

/** Market conversation: each side presses its own button, speaks, and hears the other language. */
class TalkViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application.dichApp

    var ui by mutableStateOf(TalkUi(vendorLang = app.prefs.vendorLang))
        private set

    private fun langOf(side: Side) = if (side == Side.Her) Lang.VI else ui.vendorLang

    private fun withStatus(side: Side, status: Status?) =
        if (side == Side.Her) ui.copy(herStatus = status) else ui.copy(vendorStatus = status)

    /** Press once to start listening, press again to stop early. */
    fun onMic(side: Side) {
        if (ui.listening == side) {
            app.listener.finish()
            return
        }
        app.speaker.stop()
        ui = ui.copy(listening = side, partial = "", herStatus = null, vendorStatus = null)
        ui = withStatus(side, Status.Listening)
        app.listener.start(
            lang = langOf(side),
            onPartial = { ui = ui.copy(partial = it) },
            onResult = { heard ->
                ui = withStatus(side, Status.Translating).copy(listening = null, partial = "")
                translate(side, heard)
            },
            onError = { error ->
                ui = withStatus(side, Status.Failed(error)).copy(listening = null, partial = "")
                if (app.prefs.autoRead && error == ListenError.NotHeard) {
                    if (side == Side.Her) {
                        app.speaker.speak(HER_NOT_HEARD, Lang.VI)
                    } else {
                        app.speaker.speak(VendorText.NOT_HEARD.of(ui.vendorLang), ui.vendorLang)
                    }
                }
            },
        )
    }

    private fun translate(side: Side, heard: String) {
        val from = langOf(side)
        val to = if (side == Side.Her) ui.vendorLang else Lang.VI
        viewModelScope.launch {
            val translated = try {
                app.translator.translate(heard, from, to)
            } catch (e: Exception) {
                ui = withStatus(side, Status.Failed(ListenError.NeedsInternet))
                return@launch
            }
            show(Turn(side, heard, translated, app.translator.lastEngine))
        }
    }

    /** A ready-made phrase: no speech recognition or machine translation needed. */
    fun onPhrase(phrase: Phrase) {
        app.listener.cancel()
        // A chosen phrase is meant for the other person: its own setting decides whether it is
        // said aloud in their language, independently of the general read-aloud switch.
        show(Turn(Side.Her, phrase.vi, phrase.text.of(ui.vendorLang)), speak = app.prefs.speakPhrases)
    }

    private fun show(turn: Turn, speak: Boolean = app.prefs.autoRead) {
        ui = ui.copy(last = turn, listening = null, partial = "", herStatus = null, vendorStatus = null)
        app.history.add(turn.vietnamese, turn.foreign, ui.vendorLang)
        if (speak) {
            say(turn, toward = if (turn.speaker == Side.Her) Side.Vendor else Side.Her)
        }
    }

    /** Replay the last sentence in the language of [side]. */
    fun replay(side: Side) {
        ui.last?.let { say(it, toward = side) }
    }

    private fun say(turn: Turn, toward: Side) {
        if (toward == Side.Her) {
            app.speaker.speak(turn.vietnamese, Lang.VI)
        } else {
            app.speaker.speak(turn.foreign, ui.vendorLang)
        }
    }

    fun toggleVendorLang() {
        val next = if (ui.vendorLang == Lang.FR) Lang.EN else Lang.FR
        app.prefs.vendorLang = next
        app.listener.cancel()
        ui = TalkUi(vendorLang = next)
    }

    fun stopAll() {
        app.listener.cancel()
        app.speaker.stop()
        ui = ui.copy(listening = null, partial = "")
    }

    override fun onCleared() {
        stopAll()
        app.listener.release()
    }

    companion object {
        const val HER_NOT_HEARD = "Tôi nghe không rõ. Xin nói lại."
    }
}
