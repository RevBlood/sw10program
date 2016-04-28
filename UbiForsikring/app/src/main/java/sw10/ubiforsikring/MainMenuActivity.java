package sw10.ubiforsikring;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.CarObjects.Car;

public class MainMenuActivity extends AppCompatActivity {
    final static int FINE_LOCATION_PERMISSION_REQUEST = 0;
    final static int PHONE_STATE_PERMISSION_REQUEST = 1;

    //Activity information
    Context mContext;
    boolean mIsGPSDialogOpen = false;

    //TripService communication
    BroadcastReceiver mStatusReceiver;
    ServiceConnection mTripServiceConnection;
    static Messenger mMessenger;

    //TripService status
    boolean mIsConnected = false;
    boolean mIsTripActive = false;
    boolean mIsProcessing = false;

    //region ACTIVITY EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mContext = this;
        mStatusReceiver = new StatusReceiver();

        //Verify CarId is accessible, otherwise application won't work
        CheckCarId();

        //Recover any dialogs that were open
        if (savedInstanceState != null && savedInstanceState.getBoolean(getString(R.string.IsGPSDialogOpen), false)) {
            GPSDisabledDialog().show();
        }

        //Setup buttons
        Button tripButton = (Button) findViewById(R.id.TripButton);
        tripButton.setOnClickListener(TripButtonListener);

        Button liveMapButton = (Button) findViewById(R.id.LiveMapButton);
        liveMapButton.setOnClickListener(LiveMapButtonListener);

        Button tripOverviewButton = (Button) findViewById(R.id.TripOverviewButton);
        tripOverviewButton.setOnClickListener(TripOverviewButtonListener);

        Button competitionsButton = (Button) findViewById(R.id.CompetitionsButton);
        competitionsButton.setOnClickListener(CompetitionsButtonListener);
    }

    @Override
    public void onResume() {
        //Listen for TripService status updates
        registerReceiver(mStatusReceiver, new IntentFilter(getString(R.string.BroadcastStatusIntent)));

        //Start the TripService and/or bind it to Activity
        InitializeTripService();

        super.onResume();
    }

    @Override
    public void onPause() {
        //Disconnect connection to the TripService
        unbindService(mTripServiceConnection);

        //Stop listening for status updates
        unregisterReceiver(mStatusReceiver);

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // If any dialogs are open, save that information so they can be recovered later
        super.onSaveInstanceState(outState);
        outState.putBoolean(getString(R.string.IsGPSDialogOpen), mIsGPSDialogOpen);
    }

    @Override
    public void onBackPressed() {
        //Deny application exit if trip is ongoing
        if (!mIsTripActive) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, R.string.OnBackPressedToast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create the proper layout for the Actionbar
        getMenuInflater().inflate(R.menu.main_menu_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Whenever an item on the Actionbar is clicked, check if it's Settings
        int id = item.getItemId();

        if (id == R.id.actionbar_settings) {
            startActivity(new Intent(mContext, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // If permission result is for FINE_LOCATION, save the status and start trip (This is where the permission was checked)
        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Button tripButton = (Button) findViewById(R.id.TripButton);
                tripButton.performClick();
            }
        }

        // If permission result is for PHONE_STATE, save & send to server is successful, otherwise display dialog
        // Server communication blocks the UI on purpose because no other interaction should be possible at this point
        if (requestCode == PHONE_STATE_PERMISSION_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SaveIMEI();
                // Block user from proceeding with a dialog if IMEI could not be sent.
                if(!SendIMEI()) {
                    SendIMEIFailedDialog().show();
                }
            } else {
                IMEIDeniedDialog().show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //endregion

    //region LISTENERS

    Button.OnClickListener TripButtonListener = new Button.OnClickListener() {
        public void onClick(View v) {
            //If about to start trip, verify everything is running and proper permissions are granted
            if (!mIsTripActive) {
                if(!CheckFineLocationPermission() || !CheckGPSEnabled()) {
                    return;
                }

                if (MessageTripService(TripService.BEGIN_TRIP)) {
                    Toast.makeText(mContext, R.string.TripStartToast, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (MessageTripService(TripService.END_TRIP)) {
                    Toast.makeText(mContext, R.string.TripStopToast, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    Button.OnClickListener LiveMapButtonListener = new Button.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(mContext, LiveMapActivity.class));
        }
    };

    Button.OnClickListener TripOverviewButtonListener = new Button.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(mContext, TripListActivity.class));
        }
    };

    Button.OnClickListener CompetitionsButtonListener = new Button.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(mContext, CompetitionListActivity.class));
        }
    };

    //endregion

    //region INCOMING EVENTS

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Save status variables - then handle relevant actions for these variables
            mIsConnected = intent.getBooleanExtra(getString(R.string.BroadcastIsConnected), false);
            mIsTripActive = intent.getBooleanExtra(getString(R.string.BroadcastIsTripActive), false);
            mIsProcessing = intent.getBooleanExtra(getString(R.string.BroadcastIsProcessing), false);

            HandleConnectionStatus();
            HandleTripStatus();
        }
    }

    private void HandleConnectionStatus() {
        Button tripButton = (Button) findViewById(R.id.TripButton);

        // As soon as The Trip Service is connected to Google Play Services, it is safe to start a trip
        if (mIsConnected) {
            tripButton.setEnabled(true);
        } else {
            tripButton.setEnabled(false);
        }
    }

    private void HandleTripStatus() {
        // Views to update
        Button tripButton = (Button) findViewById(R.id.TripButton);
        Button liveMapButton = (Button) findViewById(R.id.LiveMapButton);
        RelativeLayout endingTripLayout = (RelativeLayout) findViewById(R.id.TripEndingLayout);

        // If trip is active, enable the Live Map
        // If trip is processing, disable and replace Live Map with progressbar
        // If trip has ended, remove progressbar
        if (mIsTripActive) {
            tripButton.setText(R.string.TripButtonTitleStop);
            tripButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.trip_button_stop_shape));
            liveMapButton.setEnabled(true);
        } else if (mIsProcessing) {
            tripButton.setText(R.string.TripButtonTitleStopping);
            tripButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.trip_button_stopping_shape));
            liveMapButton.setEnabled(false);
            liveMapButton.setVisibility(View.GONE);
            endingTripLayout.setVisibility(View.VISIBLE);
        } else {
            tripButton.setText(R.string.TripButtonTitleStart);
            tripButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.trip_button_start_shape));
            liveMapButton.setEnabled(false);
            liveMapButton.setVisibility(View.VISIBLE);
            endingTripLayout.setVisibility(View.GONE);
        }
    }

    //endregion

    //region TRIP SERVICE
    //Method found on http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    private boolean IsServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void InitializeTripService() {
        if (!IsServiceRunning(TripService.class)) {
            startService(new Intent(this, TripService.class));
        }

        InitializeTripServiceConnection();
        BindTripService();
    }

    private void InitializeTripServiceConnection() {
        //Create a connection and a messenger for communication with the service
        //Enable/disable interaction with the service depending on connection status
        mTripServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Button tripButton = (Button) findViewById(R.id.TripButton);
                tripButton.setEnabled(true);
                mMessenger = new Messenger(service);
                MessageTripService(TripService.UPDATE_STATUS_BROADCAST);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Button tripButton = (Button) findViewById(R.id.TripButton);
                tripButton.setEnabled(false);
                mMessenger = null;
            }
        };
    }

    private void BindTripService(){
        Intent intent = new Intent(this, TripService.class);
        bindService(intent, mTripServiceConnection, Context.BIND_AUTO_CREATE);
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
    }

    //endregion

    // region PERMISSION & SETTINGS HANDLING

    private boolean CheckGPSEnabled() {
        try {
            // Return status depending on whether or not GPS is enabled and set to High Accuracy
            if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
                mIsGPSDialogOpen = true;
                GPSDisabledDialog().show();
                return false;
            }
        } catch (Settings.SettingNotFoundException e) {
            //TODO: Verify if this can even happen
        }

        return true;
    }

    private boolean CheckFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // If Fine Location permission is not given, start the request process
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
            return false;
        }
    }

    private void VerifyPhoneStatePermission() {
        // This permission MUST be given - If not, request it. If permission is granted, save and send it.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            SaveIMEI();
            if(!SendIMEI()) {
                // Block user from proceeding with a dialog if IMEI could not be sent.
                SendIMEIFailedDialog().show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION_REQUEST);
        }
    }

    private AlertDialog GPSDisabledDialog(){
        return new AlertDialog.Builder(mContext)
            .setTitle(getString(R.string.GPSDisabledDialogTitle))
            .setMessage(getString(R.string.GPSDisabledDialogText))
            .setPositiveButton(getString(R.string.GPSDisabledSettingsButtonText), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Let user go to settings to fix the GPS settings
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    mIsGPSDialogOpen = false;
                    dialog.cancel();
                }
            })
            .setNegativeButton(getString(R.string.GPSDisabledCancelButtonText), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mIsGPSDialogOpen = false;
                    dialog.cancel();
                }
            })
            .create();
    }

    private AlertDialog IMEIDeniedDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.IMEIDialogTitle))
                .setMessage(getString(R.string.IMEIDialogText))
                .setPositiveButton(getString(R.string.IMEIRetryButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Retry getting permission to access IMEI
                        VerifyPhoneStatePermission();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.IMEICancelButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the program. It must not be used without IMEI
                        finish();
                    }
                })
                .create();
    }

    private void CheckCarId() {
        //Check CarId is stored. If not, check if permission is granted to get IMEI (Needed to get CarId)
        SharedPreferences preferences = getSharedPreferences(getString(R.string.UserPreferences), Context.MODE_PRIVATE);
        if(!preferences.getBoolean(getString(R.string.CarIdStatus), false)) {
            VerifyPhoneStatePermission();
        }
    }

    private void SaveIMEI() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.UserPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        editor.putString(getString(R.string.StoredIMEI), telephonyManager.getDeviceId());
        editor.apply();
    }

    private boolean SendIMEI() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.UserPreferences), Context.MODE_PRIVATE);
        String imei = preferences.getString(getString(R.string.StoredIMEI), null);
        try {
            Car car = ServiceHelper.GetOrCreateCar(imei);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.CarIdStatus), true);
            editor.putInt(getString(R.string.StoredCarId), car.CarId);
            editor.apply();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private AlertDialog SendIMEIFailedDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.SendIMEIDialogTitle))
                .setMessage(getString(R.string.SendIMEIDialogText))
                .setPositiveButton(getString(R.string.SendIMEIRetryButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // Retry sending IMEI to server
                            SendIMEI();
                        } catch (Exception e) {
                            // Pop dialog again if it fails
                            SendIMEIFailedDialog().show();
                        }

                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.SendIMEICancelButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit program. It must not be used without IMEI
                        finish();
                    }
                })
                .create();
    }

    //endregion
}