package io.github.turskyi.visualizer

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.widget.Toast
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

class SettingsFragment : PreferenceFragmentCompat(),
    OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Add visualizer preferences, defined in the XML file in res->xml->pref_visualizer
        addPreferencesFromResource(R.xml.pref_visualizer)
        val sharedPreferences = preferenceScreen.sharedPreferences
        val prefScreen: PreferenceScreen = preferenceScreen
        val count: Int = prefScreen.preferenceCount

        // Go through all of the preferences, and set up their preference summary.
        for (i in 0 until count) {
            val p: Preference = prefScreen.getPreference(i)
            // You don't need to set up preference summaries for checkbox preferences because
            // they are already set up in xml using summaryOff and summary On
            if (p !is CheckBoxPreference) {
                val value = sharedPreferences?.getString(p.key, "")
                setPreferenceSummary(p, value)
            }
        }

        // COMPLETED (3) Add the OnPreferenceChangeListener specifically to the EditTextPreference
        // Add the preference listener which checks that the size is correct to the size preference
        val preference: Preference? = findPreference(getString(R.string.pref_size_key))
        preference?.onPreferenceChangeListener = this
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        // Figure out which preference was changed
        val preference: Preference? = key?.let { this.findPreference(it) }
        if (null != preference) {
            // Updates the summary for the preference
            if (preference !is CheckBoxPreference) {
                val value = sharedPreferences?.getString(preference.key, "")
                setPreferenceSummary(preference, value)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(
            this
        )
    }

    //    This method should try to convert the new preference value
    // to a float; if it cannot, show a helpful error message and return false. If it can be converted
    // to a float check that that float is between 0 (exclusive) and 3 (inclusive). If it isn't, show
    // an error message and return false. If it is a valid number, return true.
    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?
    ): Boolean {
        // In this context, we're using the onPreferenceChange listener for checking whether the
        // size setting was set to a valid value.
        val error = Toast.makeText(
            context,
            "Please select a number between 0.1 and 3",
            Toast.LENGTH_SHORT
        )

        // Double check that the preference is the size preference
        val sizeKey = getString(R.string.pref_size_key)
        if (preference.key == sizeKey) {
            val stringSize = newValue as String
            try {
                val size = stringSize.toFloat()
                // If the number is outside of the acceptable range, show an error.
                if (size > 3 || size <= 0) {
                    error.show()
                    return false
                }
            } catch (nfe: NumberFormatException) {
                // If whatever the user entered can't be parsed to a number, show an error
                error.show()
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
            this
        )
    }

    /**
     * Updates the summary for the preference
     *
     * @param preference The preference to be updated
     * @param value      The value that the preference was updated to
     */
    private fun setPreferenceSummary(preference: Preference?, value: String?) {
        if (preference is ListPreference) {
            // For list preferences, figure out the label of the selected value
            val listPreference: ListPreference = preference
            val prefIndex: Int = listPreference.findIndexOfValue(value)
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.summary = listPreference.entries[prefIndex]
            }
        } else if (preference is EditTextPreference) {
            // For EditTextPreferences, set the summary to the value's simple string representation.
            preference.setSummary(value)
        }
    }

}