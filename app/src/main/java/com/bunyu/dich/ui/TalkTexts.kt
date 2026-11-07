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

/** Ready-made phrases she can tap. Translated by hand, so they work offline and are exact. */
data class Phrase(val vi: String, val text: VendorText)

/** A group of phrases for one kind of outing, shown as a tab above the phrase buttons. */
data class PhraseCategory(val name: String, val phrases: List<Phrase>)

val PhraseCategories = listOf(
    PhraseCategory(
        "Đi chợ",
        listOf(
            Phrase("Bao nhiêu tiền?", VendorText("C'est combien ?", "How much is it?")),
            Phrase("Cho tôi cái này.", VendorText("Je voudrais celui-ci, s'il vous plaît.", "I'd like this one, please.")),
            Phrase("Tôi trả tiền mặt.", VendorText("Je vais payer en espèces.", "I'll pay in cash.")),
            Phrase("Có nhận tiền mặt không?", VendorText("Vous acceptez les espèces ?", "Do you accept cash?")),
            Phrase("Xin viết giá ra giấy.", VendorText("Pouvez-vous écrire le prix, s'il vous plaît ?", "Could you write down the price, please?")),
            Phrase("Xin nói chậm lại.", VendorText("Pouvez-vous parler plus lentement, s'il vous plaît ?", "Could you speak more slowly, please?")),
            Phrase("Tôi không hiểu.", VendorText("Je ne comprends pas.", "I don't understand.")),
            Phrase("Cảm ơn.", VendorText("Merci beaucoup.", "Thank you very much.")),
        ),
    ),
    PhraseCategory(
        "Tham quan",
        listOf(
            Phrase("Nhà vệ sinh ở đâu?", VendorText("Où sont les toilettes, s'il vous plaît ?", "Where is the toilet, please?")),
            Phrase("Lối ra ở đâu?", VendorText("Où est la sortie ?", "Where is the exit?")),
            Phrase("Lối vào ở đâu?", VendorText("Où est l'entrée ?", "Where is the entrance?")),
            Phrase("Vé bao nhiêu tiền?", VendorText("Combien coûte le billet ?", "How much is a ticket?")),
            Phrase("Mấy giờ đóng cửa?", VendorText("À quelle heure ça ferme ?", "What time does it close?")),
            Phrase("Tôi cần ngồi nghỉ.", VendorText("J'ai besoin de m'asseoir.", "I need to sit down.")),
            Phrase("Tôi bị lạc.", VendorText("Je suis perdue.", "I'm lost.")),
            Phrase("Xin giúp tôi.", VendorText("Pouvez-vous m'aider, s'il vous plaît ?", "Could you help me, please?")),
            Phrase("Gọi giúp tôi xe taxi.", VendorText("Pouvez-vous m'appeler un taxi ?", "Could you call me a taxi?")),
            Phrase("Trạm xe buýt ở đâu?", VendorText("Où est l'arrêt de bus ?", "Where is the bus stop?")),
            Phrase("Tôi chụp ảnh được không?", VendorText("Je peux prendre une photo ?", "May I take a photo?")),
            Phrase("Cảm ơn.", VendorText("Merci beaucoup.", "Thank you very much.")),
        ),
    ),
)

/** Finds a price like "3,50 €", "3 euros 50" or "3.50€" and formats it as "3,50 €". */
fun findPrice(text: String): String? {
    val match = Regex("""(\d+)(?:[.,](\d{1,2}))?\s*(?:€|euros?)(?:\s*(\d{1,2})\b)?""", RegexOption.IGNORE_CASE)
        .find(text) ?: return null
    val euros = match.groupValues[1]
    val cents = match.groupValues[2].ifEmpty { match.groupValues[3] }
    return if (cents.isEmpty()) "$euros €" else "$euros,${cents.padEnd(2, '0')} €"
}
