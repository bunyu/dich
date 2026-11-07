package com.bunyu.dich.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bunyu.dich.R
import com.bunyu.dich.dichApp
import com.bunyu.dich.engine.Lang
import com.bunyu.dich.engine.ListenError
import com.bunyu.dich.ui.theme.Blue
import com.bunyu.dich.ui.theme.Green

private val VendorBg = Color(0xFFE8F0FB)
private val HerBg = Color(0xFFEAF5EC)
private val Recording = Color(0xFFC62828)

/**
 * Split screen. The top half is upside down for the person standing opposite (French or English);
 * the bottom half is hers (Vietnamese).
 */
@Composable
fun TalkScreen(onBack: () -> Unit, vm: TalkViewModel = viewModel()) {
    val context = LocalContext.current
    var hasMic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMic = it }
    LaunchedEffect(Unit) { if (!hasMic) askMic.launch(Manifest.permission.RECORD_AUDIO) }
    DisposableEffect(Unit) { onDispose { vm.stopAll() } }

    val textSize = remember { context.dichApp.prefs.textSizeSp }
    val ui = vm.ui

    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        VendorHalf(
            ui = ui,
            textSize = textSize,
            onMic = { if (hasMic) vm.onMic(Side.Vendor) else askMic.launch(Manifest.permission.RECORD_AUDIO) },
            onRepeat = { vm.replay(Side.Vendor) },
            onToggleLang = vm::toggleVendorLang,
            modifier = Modifier.weight(1f).rotate(180f),
        )
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF333333)).padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack)
        }
        HerHalf(
            ui = ui,
            textSize = textSize,
            hasMic = hasMic,
            onMic = { if (hasMic) vm.onMic(Side.Her) else askMic.launch(Manifest.permission.RECORD_AUDIO) },
            onReplay = { vm.replay(Side.Her) },
            onPhrase = vm::onPhrase,
            modifier = Modifier.weight(1.25f),
        )
    }
}

@Composable
private fun VendorHalf(
    ui: TalkUi,
    textSize: Float,
    onMic: () -> Unit,
    onRepeat: () -> Unit,
    onToggleLang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = ui.vendorLang
    val listening = ui.listening == Side.Vendor
    Column(modifier.fillMaxWidth().background(VendorBg).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onToggleLang) {
                Text(
                    if (lang == Lang.FR) "FR  ·  english?" else "EN  ·  français ?",
                    fontSize = 18.sp,
                )
            }
            if (ui.last != null) {
                OutlinedButton(onClick = onRepeat) { Text(VendorText.REPEAT.of(lang), fontSize = 18.sp) }
            }
        }
        BigText(
            label = if (ui.last?.speaker == Side.Her) VendorText.SHE_SAID.of(lang) else null,
            text = when {
                listening && ui.partial.isNotEmpty() -> ui.partial
                ui.last == null -> VendorText.INTRO.of(lang)
                else -> ui.last.foreign
            },
            status = ui.vendorStatus?.let { vendorStatusText(it, lang) },
            textSize = textSize * 0.9f,
        )
        MicButton(
            text = if (listening) VendorText.LISTENING.of(lang) else VendorText.TAP_TO_SPEAK.of(lang),
            listening = listening,
            color = Blue,
            onClick = onMic,
        )
    }
}

private fun vendorStatusText(status: Status, lang: Lang): String = when (status) {
    Status.Listening -> VendorText.LISTENING.of(lang)
    Status.Translating -> VendorText.TRANSLATING.of(lang)
    is Status.Failed -> when (status.error) {
        ListenError.NotHeard -> VendorText.NOT_HEARD.of(lang)
        ListenError.NeedsInternet -> VendorText.NEEDS_INTERNET.of(lang)
        else -> VendorText.ERROR.of(lang)
    }
}

@Composable
private fun HerHalf(
    ui: TalkUi,
    textSize: Float,
    hasMic: Boolean,
    onMic: () -> Unit,
    onReplay: () -> Unit,
    onPhrase: (Phrase) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listening = ui.listening == Side.Her
    val last = ui.last
    val price = last?.takeIf { it.speaker == Side.Vendor }?.let { findPrice(it.heard) ?: findPrice(it.translated) }

    Column(modifier.fillMaxWidth().background(HerBg).padding(12.dp)) {
        if (price != null) {
            Text(
                price,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        BigText(
            label = when (last?.speaker) {
                Side.Vendor -> stringResource(R.string.talk_they_said)
                Side.Her -> stringResource(R.string.talk_you_said)
                null -> null
            },
            text = when {
                listening && ui.partial.isNotEmpty() -> ui.partial
                last == null -> stringResource(if (hasMic) R.string.talk_intro else R.string.mic_permission)
                else -> last.vietnamese
            },
            status = ui.herStatus?.let { herStatusText(it) },
            textSize = textSize,
        )
        PhraseRow(onPhrase)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MicButton(
                text = stringResource(if (listening) R.string.talk_listening else R.string.talk_press),
                listening = listening,
                color = Green,
                onClick = onMic,
                modifier = Modifier.weight(1f),
            )
            if (last != null) {
                BigButton(
                    text = stringResource(R.string.talk_replay),
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    onClick = onReplay,
                    modifier = Modifier.heightIn(min = 96.dp),
                )
            }
        }
    }
}

@Composable
private fun herStatusText(status: Status): String = when (status) {
    Status.Listening -> stringResource(R.string.talk_listening)
    Status.Translating -> stringResource(R.string.translating)
    is Status.Failed -> when (status.error) {
        ListenError.NotHeard -> TalkViewModel.HER_NOT_HEARD
        ListenError.NeedsInternet -> stringResource(R.string.talk_needs_internet)
        ListenError.NoPermission -> stringResource(R.string.mic_permission)
        ListenError.Other -> stringResource(R.string.error_generic)
    }
}

@Composable
private fun ColumnScope.BigText(label: String?, text: String, status: String?, textSize: Float) {
    Column(
        Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        if (status != null) {
            Text(status, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Recording)
            Spacer(Modifier.height(4.dp))
        }
        if (label != null) {
            Text(label, fontSize = 18.sp, color = Color.DarkGray)
        }
        Text(text, fontSize = textSize.sp, lineHeight = (textSize * 1.3f).sp, color = Color.Black)
    }
}

@Composable
private fun PhraseRow(onPhrase: (Phrase) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MarketPhrases.forEach { phrase ->
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPhrase(phrase)
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.heightIn(min = 64.dp),
            ) {
                Text(phrase.vi, fontSize = 22.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun MicButton(
    text: String,
    listening: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (listening) Recording else color,
            contentColor = Color.White,
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = 96.dp),
    ) {
        androidx.compose.material3.Icon(
            if (listening) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp).width(40.dp).height(40.dp),
        )
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

