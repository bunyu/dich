package com.bunyu.dich.engine

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/** Reads printed text from a photo, fully on-device. */
class TextReader {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /** Returns one string per block of text, with the lines of a block joined into one sentence. */
    suspend fun read(bitmap: Bitmap): List<String> {
        val result = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
        return result.textBlocks
            .map { block -> block.lines.joinToString(" ") { it.text.trim() } }
            .filter { it.isNotBlank() }
    }
}
