package jp.benkyosuruzo.ocr

import android.graphics.Bitmap
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Shared, process-wide holder for the active capture session. Written by
 * [CaptureForegroundService], read by [ScreenCaptureManager].
 */
object CaptureSession {

    @Volatile
    private var session: Session? = null

    fun markReady(s: Session) {
        release()
        session = s
    }

    fun current(): Session? = session

    fun awaitReady(timeoutMs: Long): Session? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            session?.let { return it }
            try {
                Thread.sleep(30)
            } catch (_: InterruptedException) {
                break
            }
        }
        return session
    }

    fun release() {
        session?.release()
        session = null
    }

    class Session(
        private val projection: MediaProjection,
        private val imageReader: ImageReader,
        private val virtualDisplay: VirtualDisplay
    ) {
        val isAlive: Boolean
            get() = try {
                virtualDisplay.display != null
            } catch (_: Exception) {
                false
            }

        fun acquireFrame(): Bitmap? {
            if (!isAlive) return null
            var result: Bitmap? = null
            val latch = CountDownLatch(1)
            val handler = Handler(Looper.getMainLooper())
            imageReader.setOnImageAvailableListener({ reader ->
                try {
                    val image = reader.acquireLatestImage()
                    if (image != null) {
                        result = imageToBitmap(image)
                        image.close()
                    }
                } catch (_: Exception) {
                } finally {
                    latch.countDown()
                }
            }, handler)
            latch.await(2, TimeUnit.SECONDS)
            imageReader.setOnImageAvailableListener(null, null)
            return result
        }

        fun release() {
            try { virtualDisplay.release() } catch (_: Exception) {}
            try { imageReader.close() } catch (_: Exception) {}
            try { projection.stop() } catch (_: Exception) {}
        }

        private fun imageToBitmap(image: Image): Bitmap {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * image.width
            val bitmapWidth = image.width + rowPadding / pixelStride
            val full = Bitmap.createBitmap(bitmapWidth, image.height, Bitmap.Config.ARGB_8888)
            full.copyPixelsFromBuffer(buffer)
            return if (bitmapWidth == image.width) {
                full
            } else {
                Bitmap.createBitmap(full, 0, 0, image.width, image.height)
            }
        }
    }
}
