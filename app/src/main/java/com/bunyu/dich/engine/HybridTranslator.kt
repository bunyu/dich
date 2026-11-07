package com.bunyu.dich.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/** Which engine produced the last translation, shown discreetly so the family can tell. */
enum class Engine { Gemini, GoogleCloud, Offline }

/**
 * Online first when a key is set and the phone is online: Gemini, then Google Cloud Translation.
 * If neither is set up, the phone is offline, or the online call fails for any reason, falls back
 * to on-device ML Kit so translation never stops.
 */
class HybridTranslator(
    context: Context,
    private val gemini: GeminiTranslator,
    private val cloud: GoogleCloudTranslator,
    val offline: OnDeviceTranslator,
) : TextTranslator {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)

    @Volatile
    var lastEngine: Engine = Engine.Offline
        private set

    /** Why the last online attempt failed, for the family settings; null if it worked. */
    @Volatile
    var lastOnlineError: String? = null
        private set

    /** True when the next translation will try an online engine. */
    fun willUseOnline(): Boolean = (gemini.isConfigured() || cloud.isConfigured()) && isOnline()

    override suspend fun translate(text: String, from: Lang, to: Lang): String =
        translateAll(listOf(text), from, to).first()

    override suspend fun translateAll(texts: List<String>, from: Lang, to: Lang): List<String> {
        if (from == to) return texts
        if (isOnline()) {
            if (gemini.isConfigured()) {
                try {
                    return gemini.translateAll(texts, from, to).also { done(Engine.Gemini) }
                } catch (e: Exception) {
                    failed("Gemini", e)
                }
            }
            if (cloud.isConfigured()) {
                try {
                    return cloud.translateAll(texts, from, to).also { done(Engine.GoogleCloud) }
                } catch (e: Exception) {
                    failed("Google Cloud", e)
                }
            }
        }
        return offline.translateAll(texts, from, to).also { lastEngine = Engine.Offline }
    }

    private fun done(engine: Engine) {
        lastEngine = engine
        lastOnlineError = null
    }

    private fun failed(name: String, e: Exception) {
        Log.w(TAG, "$name translation failed", e)
        lastOnlineError = "$name: ${e.message.orEmpty().take(200)}"
    }

    private fun isOnline(): Boolean {
        val caps = connectivity?.getNetworkCapabilities(connectivity.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private companion object {
        const val TAG = "HybridTranslator"
    }
}
