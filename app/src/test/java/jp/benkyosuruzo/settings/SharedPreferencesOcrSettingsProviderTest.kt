package jp.benkyosuruzo.settings

import jp.benkyosuruzo.ocr.CaptureMode
import org.junit.Assert.assertEquals
import org.junit.Test

class SharedPreferencesOcrSettingsProviderTest {

    @Test
    fun persistentModeParses() {
        assertEquals(CaptureMode.PERSISTENT, captureModeFrom("persistent"))
    }

    @Test
    fun onceModeParses() {
        assertEquals(CaptureMode.ONCE, captureModeFrom("once"))
    }

    @Test
    fun nullDefaultsToOnce() {
        assertEquals(CaptureMode.ONCE, captureModeFrom(null))
    }

    @Test
    fun unknownDefaultsToOnce() {
        assertEquals(CaptureMode.ONCE, captureModeFrom("bogus"))
    }

    @Test
    fun settingsKeysAreStable() {
        assertEquals("ocr_enabled", SettingsKeys.OCR_ENABLED)
        assertEquals("benkyo_settings", SettingsKeys.PREFS_NAME)
    }
}
