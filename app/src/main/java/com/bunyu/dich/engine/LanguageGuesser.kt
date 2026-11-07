package com.bunyu.dich.engine

import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.tasks.await

/** Tells French, English and Vietnamese apart. Anything unsure counts as French (she lives in France). */
class LanguageGuesser {
    private val identifier = LanguageIdentification.getClient()

    suspend fun guess(text: String): Lang =
        when (identifier.identifyLanguage(text).await()) {
            "vi" -> Lang.VI
            "en" -> Lang.EN
            else -> Lang.FR
        }
}
