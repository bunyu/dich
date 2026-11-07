package com.bunyu.dich

import android.app.Application
import android.content.Context
import com.bunyu.dich.engine.GoogleCloudTranslator
import com.bunyu.dich.engine.History
import com.bunyu.dich.engine.HybridTranslator
import com.bunyu.dich.engine.LanguageGuesser
import com.bunyu.dich.engine.Listener
import com.bunyu.dich.engine.OnDeviceTranslator
import com.bunyu.dich.engine.Speaker
import com.bunyu.dich.engine.TextReader

/** Holds the app-wide engines so screens share one instance of each. */
class DichApplication : Application() {
    val prefs by lazy { Prefs(this) }
    val speaker by lazy { Speaker(this, prefs) }
    val offlineTranslator by lazy { OnDeviceTranslator() }
    val onlineTranslator by lazy { GoogleCloudTranslator(this) { prefs.googleApiKey } }
    val translator by lazy { HybridTranslator(this, onlineTranslator, offlineTranslator) }
    val textReader by lazy { TextReader() }
    val languageGuesser by lazy { LanguageGuesser() }
    val listener by lazy { Listener(this) }
    val history by lazy { History(this) }
}

val Context.dichApp: DichApplication
    get() = applicationContext as DichApplication
