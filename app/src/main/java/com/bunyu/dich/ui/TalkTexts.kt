package com.bunyu.dich.ui

import com.bunyu.dich.engine.Lang

/**
 * Text shown on (or read to) the other person's half of the screen, in each language they may
 * speak. Arabic is simple Modern Standard Arabic, which everyone in Morocco reads.
 */
data class VendorText(
    val fr: String,
    val en: String,
    val ar: String,
    val ja: String,
    val zh: String,
    val ko: String,
) {
    fun of(lang: Lang): String = when (lang) {
        Lang.EN -> en
        Lang.AR -> ar
        Lang.JA -> ja
        Lang.ZH -> zh
        Lang.KO -> ko
        Lang.FR, Lang.VI -> fr
    }

    companion object {
        val INTRO = VendorText(
            fr = "Cette dame parle vietnamien. Appuyez sur le bouton et parlez.",
            en = "This lady speaks Vietnamese. Press the button and speak.",
            ar = "هذه السيدة تتكلم الفيتنامية. اضغط على الزر وتكلم.",
            ja = "この方はベトナム語を話します。ボタンを押して話してください。",
            zh = "这位女士说越南语。请按下按钮说话。",
            ko = "이분은 베트남어를 합니다. 버튼을 누르고 말씀하세요.",
        )
        val TAP_TO_SPEAK = VendorText(
            fr = "Appuyez pour parler",
            en = "Press to speak",
            ar = "اضغط للتكلم",
            ja = "押して話す",
            zh = "按下说话",
            ko = "눌러서 말하기",
        )
        val LISTENING = VendorText(
            fr = "J'écoute… appuyez pour finir",
            en = "Listening… press to finish",
            ar = "أستمع… اضغط للإنهاء",
            ja = "聞いています…終わったら押す",
            zh = "正在听…说完请按",
            ko = "듣는 중… 끝나면 누르세요",
        )
        val TRANSLATING = VendorText(
            fr = "Traduction…",
            en = "Translating…",
            ar = "جارٍ الترجمة…",
            ja = "翻訳中…",
            zh = "正在翻译…",
            ko = "번역 중…",
        )
        val NOT_HEARD = VendorText(
            fr = "Je n'ai pas compris. Répétez, s'il vous plaît.",
            en = "I didn't catch that. Please say it again.",
            ar = "لم أفهم. أعد من فضلك.",
            ja = "聞き取れませんでした。もう一度お願いします。",
            zh = "没听清楚。请再说一遍。",
            ko = "잘 못 들었어요. 다시 말씀해 주세요.",
        )
        val NEEDS_INTERNET = VendorText(
            fr = "Il faut Internet pour cette langue.",
            en = "This needs an internet connection.",
            ar = "هذه اللغة تحتاج إلى الإنترنت.",
            ja = "この言語にはインターネット接続が必要です。",
            zh = "这种语言需要连接网络。",
            ko = "이 언어는 인터넷 연결이 필요합니다.",
        )
        val ERROR = VendorText(
            fr = "Erreur. Réessayez.",
            en = "Error. Please try again.",
            ar = "خطأ. حاول مرة أخرى.",
            ja = "エラーです。もう一度お試しください。",
            zh = "出错了。请再试一次。",
            ko = "오류입니다. 다시 시도해 주세요.",
        )
        val REPEAT = VendorText(
            fr = "Répéter",
            en = "Repeat",
            ar = "إعادة",
            ja = "もう一度",
            zh = "重复",
            ko = "다시 듣기",
        )
        val SHE_SAID = VendorText(
            fr = "Elle dit :",
            en = "She says:",
            ar = "تقول:",
            ja = "この方の言葉：",
            zh = "她说：",
            ko = "이분의 말:",
        )
    }
}

/** Ready-made phrases she can tap. Translated by hand, so they work offline and are exact. */
data class Phrase(val vi: String, val text: VendorText)

/** A group of phrases for one kind of outing, shown as a tab above the phrase buttons. */
data class PhraseCategory(val name: String, val phrases: List<Phrase>)

private val THANKS = Phrase(
    "Cảm ơn.",
    VendorText("Merci beaucoup.", "Thank you very much.", "شكرًا جزيلًا.", "どうもありがとうございます。", "非常感谢。", "정말 감사합니다."),
)

val PhraseCategories = listOf(
    PhraseCategory(
        "Đi chợ",
        listOf(
            Phrase(
                "Bao nhiêu tiền?",
                VendorText("C'est combien ?", "How much is it?", "بكم هذا؟", "これはいくらですか？", "这个多少钱？", "이거 얼마예요?"),
            ),
            Phrase(
                "Cho tôi cái này.",
                VendorText(
                    "Je voudrais celui-ci, s'il vous plaît.", "I'd like this one, please.",
                    "أريد هذا من فضلك.", "これをください。", "我要这个，谢谢。", "이걸로 주세요.",
                ),
            ),
            Phrase(
                "Tôi trả tiền mặt.",
                VendorText("Je vais payer en espèces.", "I'll pay in cash.", "سأدفع نقدًا.", "現金で払います。", "我付现金。", "현금으로 낼게요."),
            ),
            Phrase(
                "Có nhận tiền mặt không?",
                VendorText(
                    "Vous acceptez les espèces ?", "Do you accept cash?",
                    "هل تقبلون الدفع نقدًا؟", "現金で払えますか？", "可以付现金吗？", "현금 되나요?",
                ),
            ),
            Phrase(
                "Xin viết giá ra giấy.",
                VendorText(
                    "Pouvez-vous écrire le prix, s'il vous plaît ?", "Could you write down the price, please?",
                    "هل يمكنك أن تكتب لي الثمن من فضلك؟", "値段を書いていただけますか？", "请把价格写下来，好吗？", "가격을 적어 주시겠어요?",
                ),
            ),
            Phrase(
                "Xin nói chậm lại.",
                VendorText(
                    "Pouvez-vous parler plus lentement, s'il vous plaît ?", "Could you speak more slowly, please?",
                    "تكلم ببطء من فضلك.", "もう少しゆっくり話していただけますか？", "请说慢一点。", "좀 천천히 말씀해 주세요.",
                ),
            ),
            Phrase(
                "Tôi không hiểu.",
                VendorText("Je ne comprends pas.", "I don't understand.", "لا أفهم.", "わかりません。", "我听不懂。", "이해가 안 돼요."),
            ),
            THANKS,
        ),
    ),
    PhraseCategory(
        "Tham quan",
        listOf(
            Phrase(
                "Nhà vệ sinh ở đâu?",
                VendorText(
                    "Où sont les toilettes, s'il vous plaît ?", "Where is the toilet, please?",
                    "أين المرحاض من فضلك؟", "お手洗いはどこですか？", "请问洗手间在哪里？", "화장실이 어디예요?",
                ),
            ),
            Phrase(
                "Lối ra ở đâu?",
                VendorText("Où est la sortie ?", "Where is the exit?", "أين المخرج؟", "出口はどこですか？", "出口在哪里？", "출구가 어디예요?"),
            ),
            Phrase(
                "Lối vào ở đâu?",
                VendorText("Où est l'entrée ?", "Where is the entrance?", "أين المدخل؟", "入口はどこですか？", "入口在哪里？", "입구가 어디예요?"),
            ),
            Phrase(
                "Vé bao nhiêu tiền?",
                VendorText("Combien coûte le billet ?", "How much is a ticket?", "بكم التذكرة؟", "チケットはいくらですか？", "门票多少钱？", "표는 얼마예요?"),
            ),
            Phrase(
                "Mấy giờ đóng cửa?",
                VendorText(
                    "À quelle heure ça ferme ?", "What time does it close?",
                    "في أي ساعة يُغلق؟", "何時に閉まりますか？", "几点关门？", "몇 시에 문을 닫아요?",
                ),
            ),
            Phrase(
                "Tôi cần ngồi nghỉ.",
                VendorText(
                    "J'ai besoin de m'asseoir.", "I need to sit down.",
                    "أحتاج إلى الجلوس.", "座って休みたいです。", "我需要坐下休息一下。", "좀 앉아서 쉬어야 해요.",
                ),
            ),
            Phrase(
                "Tôi bị lạc.",
                VendorText("Je suis perdue.", "I'm lost.", "لقد تهت.", "道に迷いました。", "我迷路了。", "길을 잃었어요."),
            ),
            Phrase(
                "Xin giúp tôi.",
                VendorText(
                    "Pouvez-vous m'aider, s'il vous plaît ?", "Could you help me, please?",
                    "هل يمكنك مساعدتي من فضلك؟", "助けていただけますか？", "请帮帮我。", "좀 도와주세요.",
                ),
            ),
            Phrase(
                "Gọi giúp tôi xe taxi.",
                VendorText(
                    "Pouvez-vous m'appeler un taxi ?", "Could you call me a taxi?",
                    "هل يمكنك أن تطلب لي سيارة أجرة؟", "タクシーを呼んでいただけますか？", "能帮我叫一辆出租车吗？", "택시 좀 불러 주시겠어요?",
                ),
            ),
            Phrase(
                "Trạm xe buýt ở đâu?",
                VendorText(
                    "Où est l'arrêt de bus ?", "Where is the bus stop?",
                    "أين محطة الحافلات؟", "バス停はどこですか？", "公交车站在哪里？", "버스 정류장이 어디예요?",
                ),
            ),
            Phrase(
                "Tôi chụp ảnh được không?",
                VendorText(
                    "Je peux prendre une photo ?", "May I take a photo?",
                    "هل يمكنني التقاط صورة؟", "写真を撮ってもいいですか？", "可以拍照吗？", "사진 찍어도 돼요?",
                ),
            ),
            THANKS,
        ),
    ),
)

/** Ready-made answers the other person can tap; she hears the Vietnamese. Translated by hand. */
val VendorReplies = listOf(
    Phrase("Vâng, có.", VendorText("Oui.", "Yes.", "نعم.", "はい。", "是的。", "네.")),
    Phrase("Không.", VendorText("Non.", "No.", "لا.", "いいえ。", "不。", "아니요.")),
    Phrase(
        "Bà muốn lấy bao nhiêu?",
        VendorText("Vous en voulez combien ?", "How many would you like?", "كم تريدين؟", "いくつにしますか？", "您要多少？", "몇 개 드릴까요?"),
    ),
    Phrase(
        "Giá này tính theo ký.",
        VendorText(
            "C'est le prix au kilo.", "That's the price per kilo.",
            "هذا ثمن الكيلو.", "これは1キロの値段です。", "这是一公斤的价格。", "이건 1킬로 가격이에요.",
        ),
    ),
    Phrase("Hết hàng rồi.", VendorText("Il n'y en a plus.", "It's sold out.", "نفدت الكمية.", "売り切れです。", "卖完了。", "다 팔렸어요.")),
    Phrase(
        "Bà còn cần gì nữa không?",
        VendorText("Autre chose ?", "Anything else?", "شيء آخر؟", "ほかに何かありますか？", "还要别的吗？", "더 필요하신 거 있으세요?"),
    ),
    Phrase(
        "Bà trả bằng thẻ hay tiền mặt?",
        VendorText(
            "Carte ou espèces ?", "Card or cash?",
            "بالبطاقة أم نقدًا؟", "カードですか、現金ですか？", "刷卡还是付现金？", "카드예요, 현금이에요?",
        ),
    ),
    Phrase("Chỉ nhận tiền mặt.", VendorText("Espèces seulement.", "Cash only.", "نقدًا فقط.", "現金のみです。", "只收现金。", "현금만 받아요.")),
    Phrase(
        "Đây là tiền thối lại của bà.",
        VendorText("Voici votre monnaie.", "Here is your change.", "تفضلي الباقي.", "おつりです。", "这是找您的钱。", "거스름돈 여기 있어요."),
    ),
    Phrase(
        "Xin bà đợi một chút.",
        VendorText("Un instant, s'il vous plaît.", "One moment, please.", "لحظة من فضلك.", "少々お待ちください。", "请稍等。", "잠시만 기다려 주세요."),
    ),
    Phrase("Đi thẳng.", VendorText("Tout droit.", "Straight ahead.", "إلى الأمام مباشرة.", "まっすぐです。", "一直往前走。", "똑바로 가세요.")),
    Phrase("Rẽ phải.", VendorText("À droite.", "On the right.", "على اليمين.", "右です。", "在右边。", "오른쪽이에요.")),
    Phrase("Rẽ trái.", VendorText("À gauche.", "On the left.", "على اليسار.", "左です。", "在左边。", "왼쪽이에요.")),
    Phrase(
        "Ở đằng này, bà đi theo tôi.",
        VendorText(
            "C'est par ici, suivez-moi.", "It's this way, follow me.",
            "من هنا، اتبعيني.", "こちらです。ついて来てください。", "这边走，请跟我来。", "이쪽이에요. 따라오세요.",
        ),
    ),
    Phrase(
        "Cảm ơn bà, tạm biệt!",
        VendorText(
            "Merci, au revoir !", "Thank you, goodbye!",
            "شكرًا، مع السلامة!", "ありがとうございました。さようなら！", "谢谢，再见！", "감사합니다. 안녕히 가세요!",
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
