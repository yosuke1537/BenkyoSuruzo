package jp.benkyosuruzo.ocr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import jp.benkyosuruzo.R

/**
 * Shows a brief explanation, then requests the MediaProjection permission.
 * The result is handed back to [OcrRuntime].
 */
class OcrPermissionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_permission)

        findViewById<Button>(R.id.button_grant).setOnClickListener {
            val intent = OcrRuntime.createPermissionIntent()
            if (intent != null) {
                try {
                    startActivityForResult(intent, REQUEST_MEDIA_PROJECTION)
                } catch (_: Exception) {
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                OcrRuntime.onProjectionPermissionResult(resultCode, data)
            }
            finish()
        }
    }

    private companion object {
        const val REQUEST_MEDIA_PROJECTION = 100
    }
}
