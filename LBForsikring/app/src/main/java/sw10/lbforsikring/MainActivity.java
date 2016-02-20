package sw10.lbforsikring;

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

import sw10.lbforsikring.Objects.MainListViewAdapter;
import sw10.lbforsikring.Objects.TripObjects.Trip;

public class MainActivity extends AppCompatActivity {;
    Context mContext;
    ArrayAdapter mMainListViewAdapter;
    List<Trip> tripList;
    BroadcastReceiver mLocationServiceListener;
    ServiceConnection mLocationServiceConnection;
    Messenger mMessenger;

    //LocationService Status
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
        trip1.TripId = 0;
        tripList.add(trip1);
        Trip trip2 = new Trip();
        trip2.TripId = 1;
        tripList.add(trip2);

        //Setup listeners for UI
        Button toggleDrivingButton = (Button) findViewById(R.id.ToggleDrivingButton);
        toggleDrivingButton.setOnClickListener(ToggleDrivingListener);

        Button openMapButton = (Button) findViewById(R.id.OpenMapButton);
        openMapButton.setOnClickListener(OpenMapListener);

        ListView mainListView = (ListView)findViewById(R.id.MainListView);
        mMainListViewAdapter = new MainListViewAdapter(this, tripList);
        mainListView.setAdapter(mMainListViewAdapter);
        mainListView.setOnItemClickListener(MainListViewListener);

        //Initialize the LocationService
        if(!IsServiceRunning(LocationService.class)) {
            Log.i("Debug", "Starting LocationService");
            InitializeLocationService();
            BindLocationService();
        } else {
            Log.i("Debug", "LocationService already running");
            BindLocationService();
        }

        //Listen for LocationService status messages
        mLocationServiceListener = new LocationServiceListener();
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationServiceListener, new IntentFilter(getString(R.string.BroadcastIntent)));
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
            //Send message to LocationService to start/stop the trip
            if (!mIsDriving) {
                BeginTrip();
                Toast.makeText(mContext, R.string.ToggleDrivingStartToast, Toast.LENGTH_SHORT).show();
            } else {
                EndTrip();
                Toast.makeText(mContext, R.string.ToggleDrivingStopToast, Toast.LENGTH_SHORT).show();
            }
        }
    };

    Button.OnClickListener OpenMapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(mContext, MapActivity.class));
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
            mIsDriving = intent.getBooleanExtra(getString(R.string.BroadcastIsDriving), false);
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
            toggleDrivingButton.setText(R.string.ToggleDrivingStop);
            openMapButton.setEnabled(true);
        } else {
            toggleDrivingButton.setText(R.string.ToggleDrivingStart);
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

    private void InitializeLocationService() {
        //Start the service
        startService(new Intent(mContext, LocationService.class));

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
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void BeginTrip() {
        //Create message to LocationService with intent to run case for BEGIN_TRIP
        Message message = Message.obtain(null, LocationService.BEGIN_TRIP, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
            Toast.makeText(mContext, R.string.ToggleDrivingStartToast, Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact LocationService");
        }
    }

    private void EndTrip() {
        //Create message to LocationService with intent to run case for END_TRIP
        Message message = Message.obtain(null, LocationService.END_TRIP, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
            Toast.makeText(mContext, R.string.ToggleDrivingStopToast, Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact LocationService");
        }
    }

    //endregion
}