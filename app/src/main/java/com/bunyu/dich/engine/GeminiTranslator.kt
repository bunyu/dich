package com.bunyu.dich.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Translation with Google Gemini (Gemini API). A key from Google AI Studio works on the free tier.
 * Gemini understands context, so broken label text and market speech come out as natural,
 * polite Vietnamese. The key is typed into the family settings or built into the APK from
 * local.properties; it is never in the source code.
 */
class GeminiTranslator(private val apiKey: () -> String?) {
    /** The first model that answered; tried first next time. */
    @Volatile
    private var workingModel: String? = null

    fun isConfigured(): Boolean = !apiKey().isNullOrBlank()

    suspend fun translateAll(texts: List<String>, from: Lang, to: Lang): List<String> =
        withContext(Dispatchers.IO) {
            val key = apiKey()?.trim().orEmpty()
            require(key.isNotEmpty()) { "No Gemini API key" }
            val models = listOfNotNull(workingModel) + MODELS.filter { it != workingModel }
            var lastError: Exception? = null
            for (model in models) {
                try {
                    return@withContext call(model, key, texts, from, to).also { workingModel = model }
                } catch (e: OnlineTranslationException) {
                    lastError = e
                    // 404: this model name is not available for the key; try the next one.
                    if (e.httpCode != 404 && e.httpCode != 400) throw e
                }
            }
            throw lastError ?: IllegalStateException("No Gemini model available")
        }

    private fun call(model: String, key: String, texts: List<String>, from: Lang, to: Lang): List<String> {
        val instruction = """
            You translate from ${NAMES.getValue(from)} to ${NAMES.getValue(to)} for an elderly Vietnamese woman who lives in France.
            The input is a JSON array of strings: text read by a phone camera from product labels, letters and signs,
            or sentences spoken at a market or while visiting.
            Return ONLY a JSON array of strings with exactly the same number of items, item i being the translation of item i.
            Use simple, natural, polite language. Keep numbers, prices, dates, weights and units exactly as written.
            Keep brand names. If camera text is broken or cut, translate its most likely meaning.
        """.trimIndent()
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", instruction))))
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", JSONArray(texts).toString())))
                )
            )
            .put(
                "generationConfig",
                JSONObject().put("temperature", 0.2).put("responseMimeType", "application/json")
            )

        val conn = URL("$BASE/$model:generateContent").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 8_000
            conn.readTimeout = 20_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("x-goog-api-key", key)
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw OnlineTranslationException(code, text.take(300))

            val answer = JSONObject(text).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
                .let { parts -> (0 until parts.length()).joinToString("") { parts.getJSONObject(it).optString("text") } }
                .trim()
                .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val array = JSONArray(answer)
            if (array.length() != texts.size) {
                throw IllegalStateException("Gemini returned ${array.length()} items for ${texts.size}")
            }
            return List(array.length()) { array.getString(it) }
        } finally {
            conn.disconnect()
        }
    }

    private companion object {
        const val BASE = "https://generativelanguage.googleapis.com/v1beta/models"

        /** Fast free-tier models, newest first; the next is tried if a name is not available. */
        val MODELS = listOf("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-2.5-flash-lite", "gemini-2.5-flash")

        val NAMES = mapOf(Lang.VI to "Vietnamese", Lang.FR to "French", Lang.EN to "English")
    }
}
