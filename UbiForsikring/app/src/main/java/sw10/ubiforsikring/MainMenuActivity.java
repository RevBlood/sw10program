package sw10.ubiforsikring;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity {
    Context mContext;

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
        LocalBroadcastManager.getInstance(this).registerReceiver(mStatusReceiver, new IntentFilter(getString(R.string.BroadcastStatusIntent)));

        //Start the TripService and/or bind it to Activity
        InitializeTripService();

        super.onResume();
    }

    @Override
    public void onPause() {
        //Disconnect connection to the TripService
        unbindService(mTripServiceConnection);

        //Stop listening for status updates
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusReceiver);

        super.onPause();
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

    //endregion

    //region LISTENERS
    Button.OnClickListener TripButtonListener = new Button.OnClickListener() {
        public void onClick(View v) {
            if(!VerifyUserId()) {
                Toast.makeText(mContext, getString(R.string.UserIdNotSetToast), Toast.LENGTH_SHORT).show();
            } else {
                //Send message to TripService to start/stop the trip
                if (!mIsTripActive) {
                    //Ensure TripService is running before starting trip
                    InitializeTripService();

                    BeginTrip();
                    Toast.makeText(mContext, R.string.TripStartToast, Toast.LENGTH_SHORT).show();
                } else {
                    EndTrip();
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
            Toast.makeText(mContext, R.string.CompetitionsToast, Toast.LENGTH_SHORT).show();
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
            HandleProcessingStatus();
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
        } else {
            tripButton.setText(R.string.TripButtonTitleStart);
            tripButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.trip_button_start_shape));
            liveMapButton.setEnabled(false);
        }
    }

    private void HandleProcessingStatus() {
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
                UpdateStatusBroadcast();
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

    private void BeginTrip() {
        //Create message to TripService with intent to run case for BEGIN_TRIP
        Message message = Message.obtain(null, TripService.BEGIN_TRIP, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
            Toast.makeText(mContext, R.string.TripStartToast, Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact TripService");
        }
    }

    private void EndTrip() {
        //Create message to TripService with intent to run case for END_TRIP
        Message message = Message.obtain(null, TripService.END_TRIP, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
            Toast.makeText(mContext, R.string.TripStopToast, Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact TripService");
        }
    }

    private void UpdateStatusBroadcast() {
        //Create message to TripService with intent to update the status broadcast
        Message message = Message.obtain(null, TripService.UPDATE_STATUS_BROADCAST, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact TripService");
        }
    }
    //endregion

    private boolean VerifyUserId() {
        int userId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.UserIdTitle), "0"));
        return userId != 0;
    }

    //TODO: Delete logcat section?
    //region LOGCAT
    private ArrayList<String> ReadLogCat() {
        ArrayList<String> lines = new ArrayList<>();

        try {
            Process process = Runtime.getRuntime().exec("logcat -d -v time Debug:v *:S");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            Log.e("Debug", "Failed to read Logcat");
        }
        return lines;
    }

    private void WriteLogCat(File file, ArrayList<String> log) {
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            for (String line : log) {
                osw.write(line + "\n");
            }

            osw.flush();
            osw.close();
        } catch (IOException e) {
            Log.e("Debug", "Unable to write Logcat to file");
        }

        //Tell the system a new file exists - Otherwise a computer might not see it
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    private void SaveTripToFile() {
        File file = GetFilePath();
        ArrayList<String> log = ReadLogCat();
        WriteLogCat(file, log);
        ClearLogCat();
        Log.i("Debug", "Wrote logfile to" + file.getAbsolutePath());
    }

    private void ClearLogCat() {
        try {
            new ProcessBuilder()
                    .command("logcat", "-c")
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException e) {
            Log.e("Debug", "Failed to clear Logcat");
        }
    }

    private File GetFilePath() {
        //Save file in folder in downloads
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        dir = new File(dir.getAbsolutePath() + R.string.LogFolder);
        dir.mkdirs();

        //Get timestamp
        Long timestamp = System.currentTimeMillis() / 1000;

        //Add filename to dir and return
        return new File(dir, R.string.LogFilename + timestamp.toString() + R.string.LogFiletype);
    }
    //endregion
}
