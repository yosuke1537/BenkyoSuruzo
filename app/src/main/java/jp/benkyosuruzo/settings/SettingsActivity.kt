package jp.benkyosuruzo.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jp.benkyosuruzo.R

/**
 * Hosts [SettingsFragment]. Uses an AppCompat theme so that
 * PreferenceFragmentCompat can render.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
}
