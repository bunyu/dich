package com.bunyu.dich.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.Voice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bunyu.dich.R
import com.bunyu.dich.dichApp
import com.bunyu.dich.engine.Lang
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Family settings: automatic reading, voice, reading speed, text size, offline data. Not meant for the main user. */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.dichApp
    val prefs = app.prefs
    val speaker = app.speaker
    val scope = rememberCoroutineScope()

    var voices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var engine by remember { mutableStateOf(speaker.engineName) }
    var selectedVoice by remember { mutableStateOf(prefs.voiceName) }
    var rate by remember { mutableFloatStateOf(prefs.speechRate) }
    var textSize by remember { mutableFloatStateOf(prefs.textSizeSp) }
    var models by remember { mutableStateOf<Set<Lang>>(emptySet()) }
    var downloading by remember { mutableStateOf(false) }
    var autoRead by remember { mutableStateOf(prefs.autoRead) }
    val canListen = remember { app.listener.isAvailable() }
    val testPhrase = stringResource(R.string.voice_test_phrase)

    LaunchedEffect(Unit) {
        // The voice engine may still be starting: wait for it briefly.
        var waited = 0
        while (!speaker.isReady() && waited < 5_000) {
            delay(250)
            waited += 250
        }
        voices = speaker.vietnameseVoices()
        engine = speaker.engineName
        models = runCatching { app.translator.downloadedLanguages() }.getOrDefault(emptySet())
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onBack)
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.auto_read), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.auto_read_hint), style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = autoRead,
                onCheckedChange = {
                    autoRead = it
                    prefs.autoRead = it
                    if (!it) speaker.stop()
                },
            )
        }

        Section(stringResource(R.string.voice_section))
        Text(stringResource(R.string.engine_in_use, engine), style = MaterialTheme.typography.bodyMedium)
        if (voices.isEmpty()) {
            Text(stringResource(R.string.voice_missing), style = MaterialTheme.typography.bodyMedium)
        }
        VoiceRow(
            name = stringResource(R.string.voice_default),
            selected = selectedVoice == null,
            onSelect = {
                selectedVoice = null
                prefs.voiceName = null
                speaker.speak(testPhrase)
            },
        )
        voices.forEach { voice ->
            VoiceRow(
                name = voice.name,
                selected = selectedVoice == voice.name,
                onSelect = {
                    selectedVoice = voice.name
                    prefs.voiceName = voice.name
                    speaker.speak(testPhrase)
                },
            )
        }
        OutlinedButton(onClick = {
            try {
                context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
            } catch (_: ActivityNotFoundException) {
                context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            }
        }) {
            Text(stringResource(R.string.open_tts_settings), style = MaterialTheme.typography.bodyMedium)
        }

        Section(stringResource(R.string.speech_rate))
        Slider(
            value = rate,
            onValueChange = { rate = it },
            onValueChangeFinished = {
                prefs.speechRate = rate
                speaker.speak(testPhrase)
            },
            valueRange = 0.5f..1.2f,
        )

        Section(stringResource(R.string.text_size))
        Slider(
            value = textSize,
            onValueChange = { textSize = it },
            onValueChangeFinished = { prefs.textSizeSp = textSize },
            valueRange = 22f..44f,
        )
        Text(stringResource(R.string.text_size_sample), fontSize = textSize.sp)

        Section(stringResource(R.string.speech_section))
        Text(
            stringResource(if (canListen) R.string.speech_ready else R.string.speech_missing),
            style = MaterialTheme.typography.bodyMedium,
        )

        Section(stringResource(R.string.models_section))
        listOf(Lang.FR, Lang.EN, Lang.VI).forEach { lang ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(lang.label), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(if (lang in models) R.string.model_ready else R.string.model_missing),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        OutlinedButton(
            enabled = !downloading,
            onClick = {
                downloading = true
                scope.launch {
                    runCatching { app.translator.ensureModels() }
                    models = runCatching { app.translator.downloadedLanguages() }.getOrDefault(models)
                    downloading = false
                }
            },
        ) {
            Text(
                stringResource(if (downloading) R.string.models_downloading else R.string.models_download),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        HorizontalDivider(Modifier.padding(top = 8.dp))
        OutlinedButton(onClick = { app.history.clear() }) {
            Text(stringResource(R.string.history_clear), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Section(title: String) {
    HorizontalDivider(Modifier.padding(top = 8.dp))
    Text(title, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun VoiceRow(name: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(name, style = MaterialTheme.typography.bodyMedium)
    }
}
