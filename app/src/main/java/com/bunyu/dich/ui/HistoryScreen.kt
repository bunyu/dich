package com.bunyu.dich.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bunyu.dich.R
import com.bunyu.dich.dichApp
import com.bunyu.dich.engine.Lang
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Past translations, newest first, so she can show or hear one again. */
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.dichApp
    val items = remember { app.history.all() }
    val textSize = remember { app.prefs.textSizeSp }
    val format = remember { SimpleDateFormat("HH:mm  dd/MM", Locale("vi", "VN")) }
    DisposableEffect(Unit) { onDispose { app.speaker.stop() } }

    Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
        BackButton(onBack)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.home_history), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyLarge)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(format.format(Date(item.time)), fontSize = 18.sp, color = Color.DarkGray)
                        Text(
                            item.vietnamese,
                            fontSize = (textSize * 0.85f).sp,
                            lineHeight = (textSize * 1.1f).sp,
                            maxLines = 6,
                        )
                        Spacer(Modifier.height(8.dp))
                        BigButton(
                            text = stringResource(R.string.read_aloud),
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            onClick = { app.speaker.speak(item.vietnamese, Lang.VI) },
                        )
                    }
                }
            }
        }
    }
}
