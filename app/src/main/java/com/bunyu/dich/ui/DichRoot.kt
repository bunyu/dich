package com.bunyu.dich.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

enum class Screen { Home, Camera, Talk, Settings }

/** Navigation is a single variable: there are only four screens, and Back always returns home. */
@Composable
fun DichRoot() {
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    val goHome = { screen = Screen.Home }

    BackHandler(enabled = screen != Screen.Home, onBack = goHome)

    when (screen) {
        Screen.Home -> HomeScreen(
            onTalk = { screen = Screen.Talk },
            onRead = { screen = Screen.Camera },
            onSettings = { screen = Screen.Settings },
        )
        Screen.Camera -> CameraScreen(onBack = goHome)
        Screen.Talk -> TalkScreen(onBack = goHome)
        Screen.Settings -> SettingsScreen(onBack = goHome)
    }
}
