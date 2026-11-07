package com.bunyu.dich.engine

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

/**
 * Google Cloud Translation (Basic, v2). Much better than on-device for Vietnamese.
 * The first 500,000 characters a month are free. The API key is typed into the family settings
 * and stays on the phone; it is never in the source code.
 *
 * If the key is restricted to this Android app in Google Cloud, Google checks the package name and
 * signing-certificate SHA-1 sent in the headers below.
 */
class GoogleCloudTranslator(context: Context, private val apiKey: () -> String?) {
    private val packageName = context.packageName
    private val certSha1: String? = signingCertSha1(context)

    fun isConfigured(): Boolean = !apiKey().isNullOrBlank()

    suspend fun translateAll(texts: List<String>, from: Lang, to: Lang): List<String> =
        withContext(Dispatchers.IO) {
            val key = apiKey()?.trim().orEmpty()
            require(key.isNotEmpty()) { "No Google API key" }
            val body = JSONObject()
                .put("q", JSONArray(texts))
                .put("source", from.code)
                .put("target", to.code)
                .put("format", "text")
            val url = URL("$ENDPOINT?key=${URLEncoder.encode(key, "UTF-8")}")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.connectTimeout = 8_000
                conn.readTimeout = 10_000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("X-Android-Package", packageName)
                certSha1?.let { conn.setRequestProperty("X-Android-Cert", it) }
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code !in 200..299) throw OnlineTranslationException(code, text.take(300))

                val translations = JSONObject(text).getJSONObject("data").getJSONArray("translations")
                List(translations.length()) { i ->
                    decodeEntities(translations.getJSONObject(i).getString("translatedText"))
                }
            } finally {
                conn.disconnect()
            }
        }

    private fun decodeEntities(s: String): String =
        if ('&' in s) Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString() else s

    private companion object {
        const val ENDPOINT = "https://translation.googleapis.com/language/translate/v2"

        @Suppress("DEPRECATION")
        fun signingCertSha1(context: Context): String? = runCatching {
            val pm = context.packageManager
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.apkContentsSigners
            } else {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray() ?: return@runCatching null
            MessageDigest.getInstance("SHA-1").digest(cert).joinToString("") { "%02X".format(it) }
        }.getOrNull()
    }
}

class OnlineTranslationException(val httpCode: Int, detail: String) :
    Exception("Google Translation HTTP $httpCode: $detail")
