package com.bunyu.dich.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/** Which engine produced the last translation, shown discreetly so the family can tell. */
enum class Engine { Online, Offline }

/**
 * Uses Google Cloud Translation when a key is set and the phone is online; otherwise, or if the
 * online call fails for any reason, falls back to on-device ML Kit so translation never stops.
 */
class HybridTranslator(
    context: Context,
    private val online: GoogleCloudTranslator,
    val offline: OnDeviceTranslator,
) : TextTranslator {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)

    @Volatile
    var lastEngine: Engine = Engine.Offline
        private set

    /** True when the next translation will go online. */
    fun willUseOnline(): Boolean = online.isConfigured() && isOnline()

    override suspend fun translate(text: String, from: Lang, to: Lang): String =
        translateAll(listOf(text), from, to).first()

    override suspend fun translateAll(texts: List<String>, from: Lang, to: Lang): List<String> {
        if (from == to) return texts
        if (willUseOnline()) {
            try {
                return online.translateAll(texts, from, to).also { lastEngine = Engine.Online }
            } catch (e: Exception) {
                Log.w(TAG, "Online translation failed, using offline", e)
            }
        }
        return offline.translateAll(texts, from, to).also { lastEngine = Engine.Offline }
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
