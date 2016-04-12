package sw10.ubiforsikring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    //TripService communication
    ServiceConnection mTripServiceConnection;
    Messenger mMessenger;
    BroadcastReceiver mStatusReceiver;

    //TripService status
    boolean mIsTripActive = true;
    boolean mIsProcessing = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusReceiver = new StatusReceiver();

        //Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //Set listeners for links
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        //preferenceScreen.findPreference(getString(R.string.UserLogoutTitle)).setOnPreferenceClickListener(OnLogoutClickListener);
        preferenceScreen.findPreference(getString(R.string.HelpSettingTitle)).setOnPreferenceClickListener(OnHelpClickListener);
        preferenceScreen.findPreference(getString(R.string.FeedbackSettingTitle)).setOnPreferenceClickListener(OnFeedbackClickListener);
        preferenceScreen.findPreference(getString(R.string.AboutSettingTitle)).setOnPreferenceClickListener(OnAboutClickListener);
    }

    @Override
    public void onResume() {
        //Update titles
        /*SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.LoginPreferences), Context.MODE_PRIVATE);
        String email = preferences.getString(getString(R.string.StoredEmail), getString(R.string.StoredEmail));
        PreferenceCategory category = (PreferenceCategory) findPreference(getString(R.string.SettingsCategoryUser));
        category.setTitle(email); */

        SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.UsernamePreferences), Context.MODE_PRIVATE);
        String username = preferences.getString(getString(R.string.StoredUsername), "");
        Preference usernamePreference = findPreference(getString(R.string.UsernameTitle));
        if (!username.isEmpty()) {
            usernamePreference.setSummary(username);
            usernamePreference.setEnabled(false);
        }

        //Listen for preference changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ToggleOfflineSettings();

        //Listen for TripService status
        getActivity().registerReceiver(mStatusReceiver, new IntentFilter(getString(R.string.BroadcastStatusIntent)));

        //Connect to the TripService
        InitializeTripServiceConnection();
        BindTripService();

        super.onResume();
    }

    @Override
    public void onPause() {
        // Stop listening for preference changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        //Disconnect from TripService
        getActivity().unbindService(mTripServiceConnection);

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

    //region LISTENERS

    private Preference.OnPreferenceClickListener OnLogoutClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (mIsTripActive || mIsProcessing) {
                Toast.makeText(getActivity(), getString(R.string.OnLogoutWhileTripActiveToast), Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.LoginPreferences), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(getString(R.string.LoginStatus), false);
                editor.remove(getString(R.string.StoredEmail));
                editor.apply();

                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }

            return true;
        }
    };

    private Preference.OnPreferenceClickListener OnHelpClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Toast.makeText(getActivity(), getString(R.string.UnfinishedFeatureText), Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private Preference.OnPreferenceClickListener OnFeedbackClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Toast.makeText(getActivity(), R.string.UnfinishedFeatureText, Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private Preference.OnPreferenceClickListener OnAboutClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Toast.makeText(getActivity(), R.string.UnfinishedFeatureText, Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    //endregion

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Only need status once - Unregister the receiver afterwards
            getActivity().unregisterReceiver(mStatusReceiver);

            mIsTripActive = intent.getBooleanExtra(getString(R.string.BroadcastIsTripActive), false);
            mIsProcessing = intent.getBooleanExtra(getString(R.string.BroadcastIsProcessing), false);
        }
    }

    //region TRIP SERVICE

    private void InitializeTripServiceConnection() {
        //Create a connection and a messenger for communication with the service
        //Enable/disable interaction with the service depending on connection status
        mTripServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMessenger = new Messenger(service);

                //As soon as the service is available, request current status
                MessageTripService(TripService.UPDATE_STATUS_BROADCAST);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMessenger = null;
            }
        };
    }

    private void BindTripService() {
        Intent intent = new Intent(getActivity(), TripService.class);
        getActivity().bindService(intent, mTripServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean MessageTripService(int messageId) {
        //Create message to TripService
        Message message = Message.obtain(null, messageId, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
            return true;
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact TripService");
            return false;
        }
    }

    //endregion
}