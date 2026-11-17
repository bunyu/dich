package com.bunyu.dich.ui

import com.bunyu.dich.engine.Lang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Every hand-written text exists in every language the other person may speak, in the right script. */
class TalkTextsTest {
    private val uiTexts = with(VendorText) {
        listOf(INTRO, TAP_TO_SPEAK, LISTENING, TRANSLATING, NOT_HEARD, NEEDS_INTERNET, ERROR, REPEAT, SHE_SAID)
    }
    private val allTexts: List<VendorText> =
        uiTexts + PhraseCategories.flatMap { c -> c.phrases.map { it.text } } + VendorReplies.map { it.text }

    private val script = mapOf(
        Lang.AR to Regex("[\u0600-\u06FF]"),
        Lang.JA to Regex("[\u3040-\u30FF\u4E00-\u9FFF]"),
        Lang.ZH to Regex("[\u4E00-\u9FFF]"),
        Lang.KO to Regex("[\uAC00-\uD7AF]"),
        Lang.FR to Regex("[A-Za-zÀ-ÿ]"),
        Lang.EN to Regex("[A-Za-z]"),
    )

    @Test
    fun everyTextIsInEveryOtherLanguage() {
        for (text in allTexts) for (lang in Lang.others) {
            val s = text.of(lang)
            assertTrue("blank ${lang.name} for ${text.en}", s.isNotBlank())
            assertTrue("${lang.name} \"$s\" (${text.en}) is not in its script", script.getValue(lang).containsMatchIn(s))
        }
    }

    @Test
    fun chineseIsNotJapanese() {
        // Kana in a Chinese text means the columns were swapped.
        for (text in allTexts) {
            assertTrue("kana in Chinese \"${text.zh}\"", !Regex("[\u3040-\u30FF]").containsMatchIn(text.zh))
        }
    }

    @Test
    fun othersExcludeVietnameseAndCoverMoroccoAndAsia() {
        assertEquals(listOf(Lang.FR, Lang.EN, Lang.AR, Lang.JA, Lang.ZH, Lang.KO), Lang.others)
        assertEquals("ar-MA", Lang.AR.speechTag)
        for (lang in Lang.entries) assertEquals(lang, Lang.fromCode(lang.code))
    }
}
