package jp.benkyosuruzo.ocr

import android.graphics.Bitmap

data class OcrBlock(val text: String, val lineTexts: List<String>)

data class OcrResult(val blocks: List<OcrBlock>)

interface OcrEngine {
    suspend fun recognize(bitmap: Bitmap, languageBcp47: String): OcrResult
    fun close()
}
