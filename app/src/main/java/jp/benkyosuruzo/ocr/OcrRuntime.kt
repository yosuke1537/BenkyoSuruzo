package jp.benkyosuruzo.ocr

import android.content.Context
import android.content.Intent
import android.util.Log
import jp.benkyosuruzo.BuildConfig
import jp.benkyosuruzo.data.AppDatabase
import jp.benkyosuruzo.data.HistoryRepository
import jp.benkyosuruzo.data.TextNormalizer
import jp.benkyosuruzo.settings.SharedPreferencesOcrSettingsProvider
import jp.benkyosuruzo.text.LanguageDetector
import jp.benkyosuruzo.text.ScreenTextHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Central OCR orchestrator: owns the capture pipeline, runs OCR on demand,
 * persists results, and feeds them into the existing log chain.
 */
object OcrRuntime {

    private const val TAG = "OcrRuntime"

    @Volatile private var initialized = false
    private var captureManager: ScreenCaptureManager? = null
    private var engine: OcrEngine? = null
    private var reader: OcrTextReader? = null
    private var policy: OcrTriggerPolicy? = null
    private var scope: CoroutineScope? = null
    private var logChain: ScreenTextHandler? = null
    private var repository: HistoryRepository? = null

    fun initialize(
        context: Context,
        scope: CoroutineScope,
        logChain: ScreenTextHandler,
        settings: OcrSettingsProvider = SharedPreferencesOcrSettingsProvider(context)
    ) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            this.scope = scope
            this.logChain = logChain
            this.captureManager = ScreenCaptureManager(context.applicationContext)
            this.engine = MlKitOcrEngine()
            this.reader = OcrTextReader(
                captureManager!!,
                engine!!,
                settings,
                LanguageDetector(),
                TextNormalizer
            )
            this.policy = OcrTriggerPolicy(settings)
            this.repository = HistoryRepository(AppDatabase.getInstance(context))
            initialized = true
        }
    }

    fun maybeRunOcr(packageName: String, accessibilityKeptCount: Int, isSensitive: Boolean) {
        if (!initialized) return
        val p = policy ?: return
        if (!p.shouldRun(packageName, accessibilityKeptCount, isSensitive)) return

        val sc = scope ?: return
        val rd = reader ?: return
        val repo = repository ?: return
        val chain = logChain ?: return

        sc.launch {
            try {
                val screenText = rd.read(packageName) ?: return@launch
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "ocr kept=${screenText.texts.size} package=$packageName")
                }
                repo.record(screenText, wasSpoken = false)
                repo.applyOcrOrigin(screenText)
                chain.onScreenText(screenText)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "OCR run failed", e)
            }
        }
    }

    fun notifyWindowChanged() {
        policy?.notifyWindowChanged()
    }

    fun createPermissionIntent(): Intent? = captureManager?.createPermissionIntent()

    fun onProjectionPermissionResult(resultCode: Int, data: Intent) {
        captureManager?.onPermissionResult(resultCode, data)
    }

    fun release() {
        synchronized(this) {
            engine?.close()
            captureManager?.release()
            engine = null
            captureManager = null
            reader = null
            policy = null
            repository = null
            logChain = null
            scope = null
            initialized = false
        }
    }
}
