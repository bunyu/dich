package com.bunyu.dich.engine

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/** Anything that can translate text. */
interface TextTranslator {
    suspend fun translate(text: String, from: Lang, to: Lang): String

    /** Translates several texts at once (one network request for the online engine). */
    suspend fun translateAll(texts: List<String>, from: Lang, to: Lang): List<String> =
        texts.map { translate(it, from, to) }
}

/** Google ML Kit translation: free, offline once each language model (about 30 MB) is downloaded. */
class OnDeviceTranslator : TextTranslator {
    private val clients = mutableMapOf<Pair<Lang, Lang>, Translator>()
    private val modelManager = RemoteModelManager.getInstance()

    private fun client(from: Lang, to: Lang): Translator = synchronized(clients) {
        clients.getOrPut(from to to) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(from.code)
                    .setTargetLanguage(to.code)
                    .build()
            )
        }
    }

    override suspend fun translate(text: String, from: Lang, to: Lang): String {
        if (from == to || text.isBlank()) return text
        val c = client(from, to)
        c.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
        return c.translate(text).await()
    }

    /** Languages whose model is already on the phone. */
    suspend fun downloadedLanguages(): Set<Lang> {
        val codes = modelManager.getDownloadedModels(TranslateRemoteModel::class.java).await()
            .map { it.language }
            .toSet()
        // English is built into ML Kit and never needs a download.
        return Lang.entries.filter { it == Lang.EN || it.code in codes }.toSet()
    }

    /** Downloads every model the app needs (French, English, Vietnamese). */
    suspend fun ensureModels() {
        val conditions = DownloadConditions.Builder().build()
        client(Lang.FR, Lang.VI).downloadModelIfNeeded(conditions).await()
        client(Lang.EN, Lang.VI).downloadModelIfNeeded(conditions).await()
        client(Lang.VI, Lang.FR).downloadModelIfNeeded(conditions).await()
        client(Lang.VI, Lang.EN).downloadModelIfNeeded(conditions).await()
    }
}
