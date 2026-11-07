package com.bunyu.dich

import android.app.Application
import android.content.Context
import com.bunyu.dich.engine.LanguageGuesser
import com.bunyu.dich.engine.OnDeviceTranslator
import com.bunyu.dich.engine.Speaker
import com.bunyu.dich.engine.TextReader

/** Holds the app-wide engines so screens share one instance of each. */
class DichApplication : Application() {
    val prefs by lazy { Prefs(this) }
    val speaker by lazy { Speaker(this, prefs) }
    val translator by lazy { OnDeviceTranslator() }
    val textReader by lazy { TextReader() }
    val languageGuesser by lazy { LanguageGuesser() }
}

val Context.dichApp: DichApplication
    get() = applicationContext as DichApplication
