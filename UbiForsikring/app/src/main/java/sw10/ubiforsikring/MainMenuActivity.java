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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity {
    Context mContext;
    boolean mIsDialogOpen = false;
    boolean mHasFineLocationPermission = false;
    final static int FINE_LOCATION_PERMISSION_REQUEST = 0;
    final static int PHONE_STATE_PERMISSION_REQUEST = 1;

    //TripService communication
    BroadcastReceiver mStatusReceiver;
    ServiceConnection mTripServiceConnection;
    static Messenger mMessenger;

    //TripService Status
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

        //Verify IMEI is accessible, otherwise application won't work
        CheckIMEI();

        if (savedInstanceState != null && savedInstanceState.getBoolean(getString(R.string.IsDialogOpen), false)) {
            BuildAlertDialog().show();
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
        super.onSaveInstanceState(outState);
        outState.putBoolean(getString(R.string.IsDialogOpen), mIsDialogOpen);
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
        //Pop the proper layout for the Actionbar
        getMenuInflater().inflate(R.menu.main_menu_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionbar_settings) {
            startActivity(new Intent(mContext, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mHasFineLocationPermission = true;
            }
        }

        if (requestCode == PHONE_STATE_PERMISSION_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SaveIMEI();
                try {
                    SendIMEI();
                } catch (Exception e) {
                    SendIMEIFailedDialog().show();
                }
            } else {
                NoIMEIDialog().show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //endregion

    //region LISTENERS
    Button.OnClickListener TripButtonListener = new Button.OnClickListener() {
        public void onClick(View v) {
            //Send message to TripService to start/stop the trip
            if (!mIsTripActive) {
                //If GPS permission is not granted, request it
                VerifyFineLocationPermission();

                //If GPS is disabled, or permission is not granted, don't start the trip
                if(!VerifyGPS() || !mHasFineLocationPermission) {
                    return;
                }

                //Ensure TripService is running before starting trip
                InitializeTripService();

                //Begin or end trip
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
            mIsConnected = intent.getBooleanExtra(getString(R.string.BroadcastIsConnected), false);
            mIsTripActive = intent.getBooleanExtra(getString(R.string.BroadcastIsTripActive), false);
            mIsProcessing = intent.getBooleanExtra(getString(R.string.BroadcastIsProcessing), false);

            HandleConnectionStatus();
            HandleTripStatus();
        }
    }

    private void HandleConnectionStatus() {
        Button tripButton = (Button) findViewById(R.id.TripButton);

        if (mIsConnected) {
            tripButton.setEnabled(true);
            Log.i("Debug", "Connected to Google Play Services");
        } else {
            tripButton.setEnabled(false);
        }
    }

    private void HandleTripStatus() {
        Button tripButton = (Button) findViewById(R.id.TripButton);
        Button liveMapButton = (Button) findViewById(R.id.LiveMapButton);

        if (mIsTripActive) {
            tripButton.setText(R.string.TripButtonTitleStop);
            tripButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.trip_button_stop_shape));
            liveMapButton.setEnabled(true);
        } else if (mIsProcessing) {
            tripButton.setText(R.string.TripButtonTitleStopping);
            tripButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.trip_button_stopping_shape));
            liveMapButton.setEnabled(false);
        } else {
            tripButton.setText(R.string.TripButtonTitleStart);
            tripButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.trip_button_start_shape));
            liveMapButton.setEnabled(false);
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
            Log.i("Debug", "Starting TripService");
            InitializeTripServiceConnection();
            startService(new Intent(this, TripService.class));
            BindTripService();
        } else {
            Log.i("Debug", "TripService already running");
            InitializeTripServiceConnection();
            BindTripService();
        }
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
            Log.e("Debug", "Failed to contact TripService");
            return false;
        }
    }

    //endregion

    private boolean VerifyGPS() {
        try {
            if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
                mIsDialogOpen = true;
                BuildAlertDialog().show();
                return false;
            }
        } catch (Settings.SettingNotFoundException e) {
            //Ignore?
        }

        return true;
    }

    private void VerifyFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
        } else {
            mHasFineLocationPermission = true;
        }
    }

    private void VerifyPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            SaveIMEI();
            try {
                SendIMEI();
            } catch (Exception e) {
                SendIMEIFailedDialog().show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION_REQUEST);
        }
    }

    private AlertDialog BuildAlertDialog(){
        return new AlertDialog.Builder(mContext)
            .setTitle(getString(R.string.GPSDisabledDialogTitle))
            .setMessage(getString(R.string.GPSDisabledDialogText))
            .setPositiveButton(getString(R.string.GPSDisabledSettingsButtonText), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    mIsDialogOpen = false;
                    dialog.cancel();
                }
            })
            .setNegativeButton(getString(R.string.GPSDisabledCancelButtonText), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mIsDialogOpen = false;
                    dialog.cancel();
                }
            })
            .create();
    }

    private AlertDialog NoIMEIDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.IMEIDialogTitle))
                .setMessage(getString(R.string.IMEIDialogText))
                .setPositiveButton(getString(R.string.IMEIRetryButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        VerifyPhoneStatePermission();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.IMEICancelButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
    }

    private void CheckIMEI(){
        //Check IMEI is accessible
        SharedPreferences preferences = getSharedPreferences(getString(R.string.IMEIPreferences), Context.MODE_PRIVATE);
        if(!preferences.getBoolean(getString(R.string.IMEIStatus), false)) {
            VerifyPhoneStatePermission();
        }
    }

    private void SaveIMEI() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.IMEIPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        editor.putBoolean(getString(R.string.IMEIStatus), true);
        editor.putString(getString(R.string.StoredIMEI), telephonyManager.getDeviceId());
        editor.apply();
    }

    private void SendIMEI() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.IMEIPreferences), Context.MODE_PRIVATE);
        String imei = preferences.getString(getString(R.string.IMEIPreferences), null);
    }

    private AlertDialog SendIMEIFailedDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.SendIMEIDialogTitle))
                .setMessage(getString(R.string.SendIMEIDialogText))
                .setPositiveButton(getString(R.string.SendIMEIRetryButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            SendIMEI();
                        } catch (Exception e) {
                            SendIMEIFailedDialog();
                        }

                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.SendIMEICancelButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
    }
}