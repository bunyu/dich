package com.bunyu.dich.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bunyu.dich.R
import com.bunyu.dich.dichApp

/** Placeholder until conversation mode (milestone M2) is built. */
@Composable
fun TalkScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val message = stringResource(R.string.talk_coming)
    LaunchedEffect(Unit) { context.dichApp.speaker.speak(message) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BackButton(onBack)
        Text(
            message,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        BackButton(onBack, Modifier.fillMaxWidth())
    }
}
