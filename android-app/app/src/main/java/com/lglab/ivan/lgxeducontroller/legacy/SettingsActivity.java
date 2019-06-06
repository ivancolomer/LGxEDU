package com.lglab.ivan.lgxeducontroller.legacy;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.EditText;

import com.lglab.ivan.lgxeducontroller.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */


public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.za
        bindPreferenceSummaryToValue(findPreference("User"));
        bindPreferenceSummaryToValue(findPreference("Password"));
        bindPreferenceSummaryToValue(findPreference("HostName"));
        bindPreferenceSummaryToValue(findPreference("Port"));
        bindPreferenceSummaryToValue(findPreference("isOnChromeBook"));
        bindPreferenceSummaryToValue(findPreference("AdminPassword"));
        bindPreferenceSummaryToValue(findPreference("pref_kiosk_mode"));
        bindPreferenceSummaryToValue(findPreference("ServerIp"));
        bindPreferenceSummaryToValue(findPreference("ServerPort"));

        /*Preference User = findPreference("User");
        User.setSummary(LGConnectionManager.getInstance().getUser());
        ((EditTextPreference)User).setText(LGConnectionManager.getInstance().getUser());

        Preference Password = findPreference("Password");
        EditText edit = ((EditTextPreference) Password).getEditText();
        String pref = edit.getTransformationMethod().getTransformation(LGConnectionManager.getInstance().getPassword(), edit).toString();
        Password.setSummary(pref);
        ((EditTextPreference)Password).setText(LGConnectionManager.getInstance().getPassword());

        Preference HostName = findPreference("HostName");
        HostName.setSummary(LGConnectionManager.getInstance().getHostname());
        ((EditTextPreference)HostName).setText(LGConnectionManager.getInstance().getHostname());

        Preference Port = findPreference("Port");
        Port.setSummary(String.valueOf(LGConnectionManager.getInstance().getPort()));
        ((EditTextPreference)Port).setText(String.valueOf(LGConnectionManager.getInstance().getPort()));*/
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);


        // Trigger the listener immediately with the preference's
        // current value.
        if (preference.getKey().equals("pref_kiosk_mode") || preference.getKey().equals("isOnChromeBook")) {
            onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false));
        } else {
            onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference.getKey().contains("Password")) {
            EditText edit = ((EditTextPreference) preference).getEditText();
            String pref = edit.getTransformationMethod().getTransformation(stringValue, edit).toString();
            preference.setSummary(pref);

        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }

        /*LGConnectionManager.getInstance().setData(
                ((EditTextPreference)findPreference("User")).getText(),
                ((EditTextPreference)findPreference("Password")).getText(),
                ((EditTextPreference)findPreference("HostName")).getText(),
                Integer.parseInt(((EditTextPreference)findPreference("Port")).getText())
        );*/

        return true;
    }

    /**
     * Això és per a que un cop entrem a settings, al tornar enrere(osigui a LGPCActivity) continui tal qual estava, és a dir,
     * que el contingut del DetailFragment sigui el mateix que abans i no estigui en blanc.
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
