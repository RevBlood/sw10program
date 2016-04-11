package sw10.ubiforsikring;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.MeasureHelper;
import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.FactObjects.Fact;
import sw10.ubiforsikring.Objects.FactObjects.SpatialTemporalInformation;

public class TripService extends Service implements ConnectionCallbacks, OnConnectionFailedListener {
    //Method Handles
    static final int BEGIN_TRIP = 0;
    static final int END_TRIP = 1;
    static final int UPDATE_STATUS_BROADCAST = 2;
    static final int UPDATE_ROUTE_BROADCAST = 3;

    //Status
    boolean mIsConnected = false;
    boolean mIsTripActive = false;
    boolean mIsProcessing = false;
    Status mIsGPSActivated = null;

    Messenger mMessenger = new Messenger(new IncomingHandler());
    Notification mDrivingNotification;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    LocationListener mLocationListener;

    //region SERVICE EVENTS
    @Override
    public void onCreate() {
        //Send the first broadcast to announce current status
        UpdateStatusBroadcast();

        //Initialize the GoogleApiClient, responsible for connecting to Google Location Services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
        mGoogleApiClient.connect();

        //Initialize desired settings for location updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(getResources().getInteger(R.integer.PositionInterval));
        mLocationRequest.setFastestInterval(getResources().getInteger(R.integer.PositionFastestInterval));
        //mLocationRequest.setMaxWaitTime(getResources().getInteger(R.integer.PositionMaxWait)); //Saves battery
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Initialize the listener for location updates
        mLocationListener = new LocationListener(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Connected to Google Play services and good to go
        mIsConnected = true;
        UpdateStatusBroadcast();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.

        Log.w("Debug", "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.

        Log.e("Debug", "Connection Failed");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    //endregion

    //region INCOMING REQUESTS
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BEGIN_TRIP:
                    BeginTrip();
                    break;
                case END_TRIP:
                    EndTrip();
                    break;
                case UPDATE_STATUS_BROADCAST:
                    UpdateStatusBroadcast();
                    break;
                case UPDATE_ROUTE_BROADCAST:
                    UpdateRouteBroadcast();
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

    private void BeginTrip() {
        //Ensure the Service is run with priority (Not killed randomly by Android when resources are scarce)
        BuildServiceNotification();
        startForeground(1, mDrivingNotification);

        //Check that permission to access location has been given
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Debug", "Missing permission: ACCESS_FINE_LOCATION");
        }

        //Start retrieving location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

        //Start the MovementTimer
        mLocationListener.UpdateMovementTimer();

        //Broadcast the new status
        mIsTripActive = true;
        UpdateStatusBroadcast();
    }

    private void EndTrip() {
        //Check that permission to access location has been given
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Debug", "Missing permission: ACCESS_FINE_LOCATION");
        }

        //Stop retrieving location updates
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }

        //Stop the LocationListener from issuing notifications and clear notification if present
        mLocationListener.DisableMovementTimer();
        mLocationListener.ClearMovementNotification();

        //Get the logged entries from the LocationListener, and clear them afterwards
        List<Location> entries = new ArrayList<>(mLocationListener.GetEntries());
        mLocationListener.ClearEntries();

        //Broadcast the updated status and begin processing the trip
        mIsTripActive = false;
        mIsProcessing = true;
        UpdateStatusBroadcast();

        //ProcessTrip(entries);
        ProcessTripRaw(entries);
    }

    private void UpdateStatusBroadcast() {
        //Save status variables to intent
        Intent intent = new Intent(this.getString(R.string.BroadcastStatusIntent));
        intent.putExtra(getString(R.string.BroadcastIsConnected), mIsConnected);
        intent.putExtra(getString(R.string.BroadcastIsTripActive), mIsTripActive);
        intent.putExtra(getString(R.string.BroadcastIsProcessing), mIsProcessing);
        intent.putExtra(getString(R.string.BroadcastIsGPSActivated), mIsGPSActivated);

        //Send the broadcast
        sendBroadcast(intent);
    }

    private void UpdateRouteBroadcast() {
        //Get the logged entries from the LocationListener
        ArrayList<Location> entries = new ArrayList<>(mLocationListener.GetEntries());

        //Send the broadcast
        Intent intent = new Intent(getString(R.string.BroadcastRouteIntent));
        intent.putParcelableArrayListExtra(getString(R.string.BroadcastRouteLocationList), entries);
        sendBroadcast(intent);
    }

    //endregion

    private void BuildServiceNotification() {
        //Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.ServiceNotificationTitle))
                .setContentText(getString(R.string.ServiceNotificationText));

        //Create intent to launch MainMenu when notification is pressed
        Intent intent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        //Build and save the notification for future use
        mDrivingNotification = notificationBuilder.build();
    }

    private void ProcessTripRaw(List<Location> entries) { 
        ArrayList<Fact> facts = new ArrayList<>();

        //int userId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.StoredEmail), getString(R.string.DefaultEmail)));

        for (Location entry : entries) {
            //facts.add(new Fact(userId, new SpatialTemporalInformation(entry)));
            facts.add(new Fact(1, new SpatialTemporalInformation(entry)));

        }

        //TODO: Stop med at crashe vores app, Lau!
        /*DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        Log.e("Debug", "Timestamp" + facts.get(1).SpatialTemporal.MPoint.getTime());
        Log.e("Debug", "Timezone: " + TimeZone.getDefault().getDisplayName());
        Log.e("Debug", "Timestamp with format" + dateFormat.format(facts.get(1).SpatialTemporal.MPoint.getTime()));
        Log.e("Debug", "Number of entries: " + entries.size()); */

        //Calculate Measures given the locations from logged data
        MeasureHelper.CalculateMeasures(facts);

        //Send the data to the server
        ServiceHelper.PostFacts(facts);

        //Finish up
        mIsProcessing = false;
        UpdateStatusBroadcast();
        stopForeground(true);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {

        }
    }

    private void ProcessTrip(List<Location> entries) {
        //Handle case: No entries in list
        if(!entries.isEmpty()) {
            //Instantiate the database
            LBDatabaseHelper DbHelper = new LBDatabaseHelper(this);
            dbWriteQueries DbWriter = new dbWriteQueries(DbHelper);

            //Handle first entry
            int dateId = MeasureHelper.DBDate(entries.get(0).getTime());
            int timeId = MeasureHelper.DBTime(entries.get(0).getTime());

            //Save entry as GPSFact
            //TODO: And do it properly
            long rowID = DbWriter.InsertLocationIntoGPS(entries.get(0));
            Log.d("Debug", Long.toString(rowID));

            //Handle remaining entries
            for(int i = 1; i < entries.size(); i++) {
                dateId = MeasureHelper.DBDate(entries.get(i).getTime());
                timeId = MeasureHelper.DBTime(entries.get(i).getTime());
                double speed = MeasureHelper.Speed(entries.get(i), entries.get(i-1));

                //Save entries as GPSFacts
                //TODO: And do it properly
                rowID = DbWriter.InsertLocationIntoGPS(entries.get(i));
                Log.d("Debug", Long.toString(rowID));
            }
        }
        try {
            Thread.sleep(3500);
        } catch(InterruptedException e) {

        }
        //Finish up
        mIsProcessing = false;
        UpdateStatusBroadcast();
        stopForeground(true);
    }
}