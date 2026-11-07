package com.bunyu.dich.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bunyu.dich.R
import com.bunyu.dich.ui.theme.Blue
import com.bunyu.dich.ui.theme.Green

/** Two big buttons filling the screen, and a smaller one for past translations. Settings hide behind a 3-second press on the small gear. */
@Composable
fun HomeScreen(onTalk: () -> Unit, onRead: () -> Unit, onHistory: () -> Unit, onSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.fillMaxWidth()) {
            HiddenSettingsButton(onOpen = onSettings, modifier = Modifier.align(Alignment.TopEnd))
        }
        HomeTile(
            text = stringResource(R.string.home_talk),
            icon = Icons.Filled.Mic,
            color = Blue,
            onClick = onTalk,
            modifier = Modifier.weight(1f),
        )
        HomeTile(
            text = stringResource(R.string.home_read),
            icon = Icons.Filled.CameraAlt,
            color = Green,
            onClick = onRead,
            modifier = Modifier.weight(1f),
        )
        BigButton(
            text = stringResource(R.string.home_history),
            icon = Icons.Filled.History,
            onClick = onHistory,
            container = Color(0xFF555555),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HomeTile(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = color,
        contentColor = Color.White,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(12.dp))
            Text(text, fontSize = 44.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Opens only after a 3-second press, so an accidental tap never lands in settings. */
@Composable
private fun HiddenSettingsButton(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Icon(
        imageVector = Icons.Filled.Settings,
        contentDescription = stringResource(R.string.settings),
        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
        modifier = modifier
            .size(40.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    val released = withTimeoutOrNull(3_000L) { waitForUpOrCancellation() }
                    if (released == null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpen()
                    }
                }
            },
    )
}
