package jp.benkyosuruzo.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * ML Kit-backed OCR engine.
 *
 * Chinese uses the bundled model (offline-capable). Japanese, Korean, and
 * Latin use the unbundled Play Services recognizers (thin client + model
 * served by Google Play Services).
 */
class MlKitOcrEngine : OcrEngine {

    private val chinese = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    private val japanese = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    private val korean = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    private val latin = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognize(bitmap: Bitmap, languageBcp47: String): OcrResult {
        val client = when {
            languageBcp47.startsWith("zh") -> chinese
            languageBcp47.startsWith("ja") -> japanese
            languageBcp47.startsWith("ko") -> korean
            else -> latin
        }
        val image = InputImage.fromBitmap(bitmap, 0)
        val text = suspendCancellableCoroutine<Text> { cont ->
            client.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        val blocks = text.textBlocks.map { block ->
            OcrBlock(text = block.text, lineTexts = block.lines.map { it.text })
        }
        return OcrResult(blocks)
    }

    override fun close() {
        chinese.close()
        japanese.close()
        korean.close()
        latin.close()
    }
}
