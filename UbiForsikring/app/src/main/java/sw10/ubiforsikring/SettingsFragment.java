package sw10.ubiforsikring;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import sw10.ubiforsikring.Helpers.ServiceHelper;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    //TripService communication
    //ServiceConnection mTripServiceConnection;
    //Messenger mMessenger;
    //BroadcastReceiver mStatusReceiver;

    //TripService status
    //boolean mIsTripActive = true;
    //boolean mIsProcessing = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mStatusReceiver = new StatusReceiver();

        //Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //Set listeners for links
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        //preferenceScreen.findPreference(getString(R.string.UserLogoutTitle)).setOnPreferenceClickListener(OnLogoutClickListener);
        //preferenceScreen.findPreference(getString(R.string.HelpSettingTitle)).setOnPreferenceClickListener(OnHelpClickListener);
        //preferenceScreen.findPreference(getString(R.string.FeedbackSettingTitle)).setOnPreferenceClickListener(OnFeedbackClickListener);
        //preferenceScreen.findPreference(getString(R.string.AboutSettingTitle)).setOnPreferenceClickListener(OnAboutClickListener);
    }

    @Override
    public void onResume() {
        //Update titles
        /*SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.LoginPreferences), Context.MODE_PRIVATE);
        String email = preferences.getString(getString(R.string.StoredEmail), getString(R.string.StoredEmail));
        PreferenceCategory category = (PreferenceCategory) findPreference(getString(R.string.SettingsCategoryUser));
        category.setTitle(email); */

        //Listen for preference changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        //Update fields depending on preferences
        //ToggleOfflineSettings();
        SetUsernameSettings();

        //Listen for TripService status
        //getActivity().registerReceiver(mStatusReceiver, new IntentFilter(getString(R.string.BroadcastStatusIntent)));

        //Connect to the TripService
        //InitializeTripServiceConnection();
        //BindTripService();

        super.onResume();
    }

    @Override
    public void onPause() {
        // Stop listening for preference changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        //Disconnect from TripService
        //getActivity().unbindService(mTripServiceConnection);

        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*if(key.equals(getString(R.string.SyncOnlineSettingTitle))) {
            ToggleOfflineSettings();
        } */

        if(key.equals(getString(R.string.UsernameTitle))) {
            String username = sharedPreferences.getString(getString(R.string.UsernameTitle), "");
            if (username.isEmpty()) {
                return;
            }

            try {
                SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.SW10Preferences), Context.MODE_PRIVATE);
                int userId = preferences.getInt(getString(R.string.StoredCarId), -1);

                ServiceHelper.UpdateCarWithUsername(userId, username);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.putString(getString(R.string.UsernameTitle), username).apply();
                SetUsernameSettings();
            } catch (Exception e) {
                SendUsernameFailedDialog(username).show();
            }
        }
    }

    private void SetUsernameSettings() {
        //Check if the settings should be disabled
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = preferences.getString(getString(R.string.UsernameTitle), "");
        Preference usernamePreference = findPreference(getString(R.string.UsernameTitle));
        if (!username.isEmpty()) {
            usernamePreference.setSummary(username);
            usernamePreference.setEnabled(false);
        }
    }

    /*private void ToggleOfflineSettings() {
        //Check if offline settings should be enabled or disabled
        SwitchPreference syncOnlineSetting = (SwitchPreference) getPreferenceScreen().findPreference(getString(R.string.SyncOnlineSettingTitle));
        if (syncOnlineSetting.isChecked()) {
            getPreferenceScreen().findPreference(getString(R.string.DeleteAfterSettingTitle)).setEnabled(false);
        } else {
            getPreferenceScreen().findPreference(getString(R.string.DeleteAfterSettingTitle)).setEnabled(true);
        }
    } */

    //region LISTENERS

    /*private Preference.OnPreferenceClickListener OnLogoutClickListener = new Preference.OnPreferenceClickListener() {
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
    }; */

    /*private Preference.OnPreferenceClickListener OnHelpClickListener = new Preference.OnPreferenceClickListener() {
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
    }; */

    //endregion

    /*private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Only need status once - Unregister the receiver afterwards
            getActivity().unregisterReceiver(mStatusReceiver);

            mIsTripActive = intent.getBooleanExtra(getString(R.string.BroadcastIsTripActive), false);
            mIsProcessing = intent.getBooleanExtra(getString(R.string.BroadcastIsProcessing), false);
        }
    } */

    //region TRIP SERVICE

    /* private void InitializeTripServiceConnection() {
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
            return false;
        }
    } */

    //endregion

    private AlertDialog SendUsernameFailedDialog(final String username){
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.UsernameSendErrorText))
                .setPositiveButton(getString(R.string.TripListRetryLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.SW10Preferences), Context.MODE_PRIVATE);
                            int userId = preferences.getInt(getString(R.string.StoredCarId), -1);

                            ServiceHelper.UpdateCarWithUsername(userId, username);

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                            editor.putString(getString(R.string.UsernameTitle), username).apply();
                        } catch (Exception e) {
                            SendUsernameFailedDialog(username).show();
                        }

                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.DialogIgnore), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }
}