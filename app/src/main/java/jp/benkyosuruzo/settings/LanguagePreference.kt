package jp.benkyosuruzo.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import androidx.preference.Preference
import jp.benkyosuruzo.R

/**
 * Language picker. Shows a summary hint about model availability: Chinese is
 * bundled (offline-capable); every other language is unbundled and downloads
 * on first use.
 */
class LanguagePreference(context: Context, attrs: AttributeSet?) : ListPreference(context, attrs) {

    init {
        entries = context.resources.getStringArray(R.array.language_entries)
        entryValues = context.resources.getStringArray(R.array.language_values)

        summaryProvider = Preference.SummaryProvider<Preference> { pref ->
            val list = pref as ListPreference
            if (list.value == null || list.value == "zh-Hans") {
                context.getString(R.string.pref_language_bundled_summary)
            } else {
                context.getString(R.string.pref_language_unbundled_summary)
            }
        }
    }
}
