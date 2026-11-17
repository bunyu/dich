package com.bunyu.dich.engine

import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.tasks.await

/** Tells the app's languages apart. Anything unsure counts as French (she lives in France). */
class LanguageGuesser {
    private val identifier = LanguageIdentification.getClient()

    /** ML Kit answers "vi", "fr", "ar", "ja", "zh", "ko"…, or "und" when unsure. */
    suspend fun guess(text: String): Lang =
        Lang.fromCode(identifier.identifyLanguage(text).await()) ?: Lang.FR
}
