package jp.benkyosuruzo.settings

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import jp.benkyosuruzo.R

/**
 * Multi-choice list of installed apps to allow for OCR. Persists the selected
 * package names as a StringSet.
 */
class AppAllowListPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    override fun onAttached() {
        super.onAttached()
        updateSummary()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        updateSummary()
    }

    override fun onClick() {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.packageName != context.packageName }
            .sortedBy { it.loadLabel(pm).toString() }

        val labels = apps.map { it.loadLabel(pm).toString() }.toTypedArray()
        val packages = apps.map { it.packageName }.toTypedArray()
        val allowed = getSharedPreferences()
            ?.getStringSet(SettingsKeys.ALLOWED_PACKAGES, emptySet())
            ?.toSet()
            ?: emptySet()
        val checked = BooleanArray(apps.size) { i -> packages[i] in allowed }

        AlertDialog.Builder(context)
            .setTitle(R.string.pref_allowed_apps_title)
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val selected = HashSet<String>()
                checked.forEachIndexed { i, c -> if (c) selected.add(packages[i]) }
                persistStringSet(selected)
                updateSummary()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateSummary() {
        val count = getSharedPreferences()
            ?.getStringSet(SettingsKeys.ALLOWED_PACKAGES, emptySet())
            ?.size
            ?: 0
        summary = context.getString(R.string.pref_allowed_apps_summary_count, count)
    }
}
