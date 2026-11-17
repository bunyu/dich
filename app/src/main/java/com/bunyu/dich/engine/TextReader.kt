package com.bunyu.dich.engine

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Reads printed text from a photo, fully on-device. Latin script (French, English, Vietnamese)
 * plus Japanese, Chinese and Korean. ML Kit has no Arabic text recognizer, so Arabic labels
 * cannot be read from the camera.
 */
class TextReader {
    private val latin = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val japanese = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    private val chinese = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    private val korean = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    /** Returns one string per block of text, with the lines of a block joined into one sentence. */
    suspend fun read(bitmap: Bitmap): List<String> = coroutineScope {
        val image = InputImage.fromBitmap(bitmap, 0)
        val (lat, ja, zh, ko) = listOf(latin, japanese, chinese, korean)
            .map { r -> async { runCatching { blocks(r, image) }.getOrDefault(emptyList()) } }
            .awaitAll()
        // Each Asian recognizer also reads Latin letters, but the Latin one reads accents best.
        // Use an Asian result only when it really contains its own script.
        when {
            count(ko, HANGUL) > 0 -> ko
            count(ja, KANA) > 0 -> ja
            count(zh, HAN) > 0 -> zh
            lat.isEmpty() -> ja.ifEmpty { zh }.ifEmpty { ko }
            else -> lat
        }
    }

    private suspend fun blocks(recognizer: TextRecognizer, image: InputImage): List<String> {
        val result: Text = recognizer.process(image).await()
        return result.textBlocks
            .map { block ->
                block.lines.map { it.text.trim() }.reduceOrNull { a, b ->
                    // Chinese, Japanese and Korean lines run on without a space.
                    if (a.isNotEmpty() && b.isNotEmpty() && isCjk(a.last()) && isCjk(b.first())) a + b else "$a $b"
                }.orEmpty()
            }
            .filter { it.isNotBlank() }
    }

    private fun count(blocks: List<String>, script: Regex) = blocks.sumOf { script.findAll(it).count() }

    private fun isCjk(c: Char) = HAN.matches(c.toString()) || KANA.matches(c.toString())

    private companion object {
        val HANGUL = Regex("[\\uAC00-\\uD7AF\\u1100-\\u11FF]")
        val KANA = Regex("[\\u3040-\\u30FF]")
        val HAN = Regex("[\\u4E00-\\u9FFF\\u3400-\\u4DBF]")
    }
}
