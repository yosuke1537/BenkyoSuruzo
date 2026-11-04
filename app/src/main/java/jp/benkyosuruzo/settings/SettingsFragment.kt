package jp.benkyosuruzo.settings

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import jp.benkyosuruzo.R
import jp.benkyosuruzo.mask.MaskNotificationHelper
import jp.benkyosuruzo.service.ScreenTextReaderService

/**
 * Settings screen. Writes to the same SharedPreferences file that
 * [SharedPreferencesOcrSettingsProvider] reads, so changes take effect
 * immediately. Also manages the mask-mode notification permission flow and
 * the floating mask button.
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

    private lateinit var floatingButtonPref: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsKeys.PREFS_NAME
        setPreferencesFromResource(R.xml.pref_settings, rootKey)

        floatingButtonPref =
            findPreference<SwitchPreferenceCompat>(SettingsKeys.MASK_FLOATING_BUTTON_ENABLED)!!

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
                floatingButtonPref.isEnabled = enabled
                true
            }

        floatingButtonPref.setOnPreferenceChangeListener { _, _ ->
            // Defer until the new value is persisted, then sync the button.
            requireView().post { ScreenTextReaderService.current()?.syncFloatingButton() }
            true
        }

        syncFloatingButtonEnabledState()
    }

    override fun onResume() {
        super.onResume()
        MaskNotificationHelper.sync(requireContext())
        syncFloatingButtonEnabledState()
        ScreenTextReaderService.current()?.syncFloatingButton()
    }

    private fun syncFloatingButtonEnabledState() {
        val maskEnabled = preferenceManager.sharedPreferences
            ?.getBoolean(SettingsKeys.MASK_FEATURE_ENABLED, false)
            ?: false
        floatingButtonPref.isEnabled = maskEnabled
    }
}
