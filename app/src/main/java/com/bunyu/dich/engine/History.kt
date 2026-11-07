package com.bunyu.dich.engine

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** One past translation, kept so she can show it again later. */
data class HistoryItem(
    val time: Long,
    val vietnamese: String,
    val other: String,
    val otherLang: Lang,
)

/** The last [MAX] translations, newest first, stored on the phone only. */
class History(context: Context) {
    private val sp = context.getSharedPreferences("dich_history", Context.MODE_PRIVATE)

    fun all(): List<HistoryItem> {
        val array = runCatching { JSONArray(sp.getString(KEY, "[]")) }.getOrDefault(JSONArray())
        return (0 until array.length()).mapNotNull { i ->
            val o = array.optJSONObject(i) ?: return@mapNotNull null
            HistoryItem(
                time = o.optLong("t"),
                vietnamese = o.optString("vi"),
                other = o.optString("o"),
                otherLang = Lang.entries.firstOrNull { it.name == o.optString("l") } ?: Lang.FR,
            )
        }
    }

    fun add(vietnamese: String, other: String, otherLang: Lang) {
        if (vietnamese.isBlank()) return
        val item = HistoryItem(System.currentTimeMillis(), vietnamese, other, otherLang)
        val items = (listOf(item) + all()).take(MAX)
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("t", it.time)
                    .put("vi", it.vietnamese)
                    .put("o", it.other)
                    .put("l", it.otherLang.name)
            )
        }
        sp.edit().putString(KEY, array.toString()).apply()
    }

    fun clear() {
        sp.edit().remove(KEY).apply()
    }

    private companion object {
        const val KEY = "items"
        const val MAX = 50
    }
}
