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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Objects.TripObjects.Trip;

public class MainActivity extends AppCompatActivity {;
    Context mContext;
    ArrayAdapter mMainListViewAdapter;
    List<Trip> tripList;
    BroadcastReceiver mLocationServiceListener;
    ServiceConnection mLocationServiceConnection;
    static Messenger mMessenger;

    //TripService Status
    boolean mIsConnected = false;
    boolean mIsDriving = false;
    boolean mIsProcessing = false;

    //region ACTIVITY EVENTS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        tripList = new ArrayList<>();

        //TODO: Remove test data
        Trip trip1 = new Trip();
        trip1.TripId = 1;
        tripList.add(trip1);
        Trip trip2 = new Trip();
        trip2.TripId = 2;
        tripList.add(0, trip2);

        //Setup buttons
        Button toggleDrivingButton = (Button) findViewById(R.id.ToggleDrivingButton);
        toggleDrivingButton.setOnClickListener(ToggleDrivingListener);

        Button openMapButton = (Button) findViewById(R.id.OpenMapButton);
        openMapButton.setOnClickListener(OpenMapListener);

        //Setup ListView
        ListView mainListView = (ListView) findViewById(R.id.MainListView);
        mMainListViewAdapter = new MainListViewAdapter(this, tripList);
        mainListView.setAdapter(mMainListViewAdapter);
        mainListView.setOnItemClickListener(MainListViewListener);
        mainListView.setEmptyView(findViewById(R.id.MainListViewEmpty));

        //Listen for TripService status messages
        mLocationServiceListener = new LocationServiceListener();
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationServiceListener, new IntentFilter(getString(R.string.BroadcastStatusIntent)));

        //Initialize the TripService
        if (!IsServiceRunning(TripService.class)) {
            Log.i("Debug", "Starting TripService");
            InitializeLocationServiceConnection();
            startService(new Intent(mContext, TripService.class));
            BindLocationService();
        } else {
            Log.i("Debug", "TripService already running");
            InitializeLocationServiceConnection();
            BindLocationService();
            UpdateBroadcast();
        }
    }

    @Override
    public void onDestroy() {
        //If a trip was ongoing, make sure it is ended properly before application exit
        if (mIsDriving) {
            EndTrip();
            SaveTripToFile();
            Log.i("Debug", "Ended trip on application exit");
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //Deny application exit if trip is ongoing
        if (!mIsDriving) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, R.string.OnBackPressedToast, Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region LISTENERS
    Button.OnClickListener ToggleDrivingListener = new Button.OnClickListener() {
        public void onClick(View v) {
            //Send message to TripService to start/stop the trip
            if (!mIsDriving) {
                BeginTrip();
                Toast.makeText(mContext, R.string.TripStartToast, Toast.LENGTH_SHORT).show();
            } else {
                EndTrip();
                Toast.makeText(mContext, R.string.TripStopToast, Toast.LENGTH_SHORT).show();
            }
        }
    };

    Button.OnClickListener OpenMapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(mContext, LiveMapActivity.class));
        }
    };

    ListView.OnItemClickListener MainListViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> a, View v, int position, long id) {
            Toast.makeText(getBaseContext(), "Click", Toast.LENGTH_SHORT).show();
        }
    };

    private class LocationServiceListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsConnected = intent.getBooleanExtra(getString(R.string.BroadcastIsConnected), false);
            mIsDriving = intent.getBooleanExtra(getString(R.string.BroadcastIsTripActive), false);
            mIsProcessing = intent.getBooleanExtra(getString(R.string.BroadcastIsProcessing), false);

            HandleConnectionStatus();
            HandleDrivingStatus();
            HandleProcessingStatus();
        }
    }
    //endregion

    //region STATUS HANDLERS
    private void HandleConnectionStatus() {
        Button toggleDrivingButton = (Button) findViewById(R.id.ToggleDrivingButton);

        if (mIsConnected) {
            toggleDrivingButton.setEnabled(true);
            Log.i("Debug", "Connected to Google Play Services");
        } else {
            toggleDrivingButton.setEnabled(false);
        }
    }

    private void HandleDrivingStatus() {
        Button toggleDrivingButton = (Button) findViewById(R.id.ToggleDrivingButton);
        Button openMapButton = (Button) findViewById(R.id.OpenMapButton);

        if (mIsDriving) {
            toggleDrivingButton.setText(R.string.TripButtonTitleStop);
            openMapButton.setEnabled(true);

            //TODO: Remove test shit
            Trip trip3 = new Trip();
            trip3.TripId = 3;
            trip3.IsActive = true;
            tripList.add(0, trip3);
            mMainListViewAdapter.notifyDataSetChanged();
        } else {
            toggleDrivingButton.setText(R.string.TripButtonTitleStart);
            openMapButton.setEnabled(false);
        }
    }

    private void HandleProcessingStatus() {

    }
    //endregion

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

    //region LOCATION SERVICE
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

    private void InitializeLocationServiceConnection() {
        //Create a connection and a messenger for communication with the service
        //Enable/disable interaction with the service depending on connection status
        mLocationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Button toggleDrivingButton = (Button) findViewById(R.id.ToggleDrivingButton);
                toggleDrivingButton.setEnabled(true);
                mMessenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Button toggleDrivingButton = (Button) findViewById(R.id.ToggleDrivingButton);
                toggleDrivingButton.setEnabled(false);
                mMessenger = null;
            }
        };
    }

    private void BindLocationService(){
        Intent intent = new Intent(this, TripService.class);
        bindService(intent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
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

    private void UpdateBroadcast() {
        //Create message to TripService with intent to run case for END_TRIP
        Message message = Message.obtain(null, TripService.UPDATE_STATUS_BROADCAST, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact TripService");
        }
    }
    //endregion
}