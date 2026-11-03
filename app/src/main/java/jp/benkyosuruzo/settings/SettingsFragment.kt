package jp.benkyosuruzo.settings

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import jp.benkyosuruzo.R
import jp.benkyosuruzo.mask.MaskNotificationHelper

/**
 * Settings screen. Writes to the same SharedPreferences file that
 * [SharedPreferencesOcrSettingsProvider] reads, so changes take effect
 * immediately. Also manages the mask-mode notification permission flow.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                MaskNotificationHelper.sync(requireContext())
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.mask_notification_permission_denied,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsKeys.PREFS_NAME
        setPreferencesFromResource(R.xml.pref_settings, rootKey)

        findPreference<SwitchPreferenceCompat>(SettingsKeys.MASK_FEATURE_ENABLED)
            ?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                if (enabled) {
                    if (MaskNotificationHelper.hasNotificationPermission(requireContext())) {
                        MaskNotificationHelper.sync(requireContext())
                    } else {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    MaskNotificationHelper.cancel(requireContext())
                }
                true
            }
    }

    override fun onResume() {
        super.onResume()
        MaskNotificationHelper.sync(requireContext())
    }
}
