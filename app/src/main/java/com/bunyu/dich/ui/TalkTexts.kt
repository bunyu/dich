package com.bunyu.dich.ui

import com.bunyu.dich.engine.Lang

/** Text shown on the other person's half of the screen, in French or English. */
data class VendorText(val fr: String, val en: String) {
    fun of(lang: Lang) = if (lang == Lang.EN) en else fr

    companion object {
        val INTRO = VendorText(
            "Cette dame parle vietnamien. Appuyez sur le bouton et parlez.",
            "This lady speaks Vietnamese. Press the button and speak.",
        )
        val TAP_TO_SPEAK = VendorText("Appuyez pour parler", "Press to speak")
        val LISTENING = VendorText("J'écoute… appuyez pour finir", "Listening… press to finish")
        val TRANSLATING = VendorText("Traduction…", "Translating…")
        val NOT_HEARD = VendorText(
            "Je n'ai pas compris. Répétez, s'il vous plaît.",
            "I didn't catch that. Please say it again.",
        )
        val NEEDS_INTERNET = VendorText(
            "Il faut Internet pour cette langue.",
            "This needs an internet connection.",
        )
        val ERROR = VendorText("Erreur. Réessayez.", "Error. Please try again.")
        val REPEAT = VendorText("Répéter", "Repeat")
        val SHE_SAID = VendorText("Elle dit :", "She says:")
    }
}

/** Ready-made phrases she can tap at the market. Translated by hand, so they work offline and are exact. */
data class Phrase(val vi: String, val text: VendorText)

val MarketPhrases = listOf(
    Phrase("Bao nhiêu tiền?", VendorText("C'est combien ?", "How much is it?")),
    Phrase("Cho tôi cái này.", VendorText("Je voudrais celui-ci, s'il vous plaît.", "I'd like this one, please.")),
    Phrase("Xin viết giá ra giấy.", VendorText("Pouvez-vous écrire le prix, s'il vous plaît ?", "Could you write down the price, please?")),
    Phrase("Xin nói chậm lại.", VendorText("Pouvez-vous parler plus lentement, s'il vous plaît ?", "Could you speak more slowly, please?")),
    Phrase("Tôi không hiểu.", VendorText("Je ne comprends pas.", "I don't understand.")),
    Phrase("Cảm ơn.", VendorText("Merci beaucoup.", "Thank you very much.")),
)

/** Finds a price like "3,50 €", "3 euros 50" or "3.50€" and formats it as "3,50 €". */
fun findPrice(text: String): String? {
    val match = Regex("""(\d+)(?:[.,](\d{1,2}))?\s*(?:€|euros?)(?:\s*(\d{1,2})\b)?""", RegexOption.IGNORE_CASE)
        .find(text) ?: return null
    val euros = match.groupValues[1]
    val cents = match.groupValues[2].ifEmpty { match.groupValues[3] }
    return if (cents.isEmpty()) "$euros €" else "$euros,${cents.padEnd(2, '0')} €"
}
