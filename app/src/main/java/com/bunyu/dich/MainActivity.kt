package com.bunyu.dich

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.bunyu.dich.ui.DichRoot
import com.bunyu.dich.ui.theme.DichTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the voice engine early so the first sentence is not delayed.
        dichApp.speaker
        // Fetch the offline translation models in the background (needs internet once).
        lifecycleScope.launch {
            runCatching { dichApp.offlineTranslator.ensureModels() }
        }

        setContent {
            DichTheme {
                DichRoot()
            }
        }
    }
}
