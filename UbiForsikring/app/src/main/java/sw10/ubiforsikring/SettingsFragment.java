package sw10.ubiforsikring;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //Set listeners for links
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.findPreference(getString(R.string.HelpSettingTitle)).setOnPreferenceClickListener(OnHelpClickListener);
        preferenceScreen.findPreference(getString(R.string.FeedbackSettingTitle)).setOnPreferenceClickListener(OnFeedbackClickListener);
        preferenceScreen.findPreference(getString(R.string.AboutSettingTitle)).setOnPreferenceClickListener(OnAboutClickListener);
    }

    @Override
    public void onResume() {
        //Listen for preference changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ToggleOfflineSettings();

        super.onResume();
    }

    @Override
    public void onPause() {
        // Stop listening for preference changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.SyncOnlineSettingTitle))) {
            ToggleOfflineSettings();
        }
    }

    private void ToggleOfflineSettings() {
        //Check if offline settings should be enabled or disabled
        SwitchPreference syncOnlineSetting = (SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.SyncOnlineSettingTitle));
        if (syncOnlineSetting.isChecked()) {
            getPreferenceScreen().findPreference(getString(R.string.DeleteAfterSettingTitle)).setEnabled(false);
        } else {
            getPreferenceScreen().findPreference(getString(R.string.DeleteAfterSettingTitle)).setEnabled(true);
        }
    }

    private Preference.OnPreferenceClickListener OnHelpClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Toast.makeText(getActivity(), getString(R.string.CompetitionsToast), Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private Preference.OnPreferenceClickListener OnFeedbackClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Toast.makeText(getActivity(), R.string.CompetitionsToast, Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private Preference.OnPreferenceClickListener OnAboutClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Toast.makeText(getActivity(), R.string.CompetitionsToast, Toast.LENGTH_SHORT).show();
            return true;
        }
    };
}