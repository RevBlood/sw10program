package sw10.ubiforsikring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LiveMapActivity extends FragmentActivity implements OnMapReadyCallback {
    //TripService communication
    ServiceConnection mTripServiceConnection;
    Messenger mMessenger;
    BroadcastReceiver mRouteReceiver;
    BroadcastReceiver mLocationReceiver;

    //Map
    GoogleMap mMap;
    List<LatLng>  mRoute;
    Polyline mRouteLine;
    PolylineOptions mRouteOptions;
    MarkerOptions mMarkerOptions;
    Marker mMarker;
    GoogleMap.CancelableCallback mAnimationCallback;
    boolean mKeepAnimating = true;

    //Stats
    Long mTripStartTime;
    double mTripDistance = 0;
    SimpleDateFormat mSdf;
    Handler mTripTimer;
    Runnable mTimerTask;

    //region ACTIVITY EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);
        //Initialize variables for later use
        mSdf = new SimpleDateFormat(getString(R.string.LiveTimeText));
        mTripTimer = new Handler();
        mRoute = new ArrayList<>();
        mAnimationCallback = new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }
            @Override
            public void onCancel() {
                mKeepAnimating = false;
            }
        };
        mTimerTask = new Runnable() {
            @Override
            public void run() {
                //Get time since start of trip in UnixTime. Subtract one hour, so timer starts at 0
                long elapsedTime = System.currentTimeMillis() - mTripStartTime - 3600000;

                //Format the time and put it in the TextView
                String formattedElapsedTime = mSdf.format(elapsedTime);
                TextView liveTimeView = (TextView) findViewById(R.id.LiveTimeView);
                liveTimeView.setText(formattedElapsedTime);

                //Run the task each second
                mTripTimer.postDelayed(this, 1000);
            }
        };

        //Define how the route looks on the map
        mRouteOptions = new PolylineOptions();
        mRouteOptions.color(ContextCompat.getColor(this, R.color.colorPrimary));
        mRouteOptions.width(getResources().getInteger(R.integer.LiveGpsRouteWidth));

        //Define how the position marker looks
        mMarkerOptions = new MarkerOptions();
        mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mMarkerOptions.anchor(0.5f, 0.5f);

        //Get the map ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.LiveMap);
        mapFragment.getMapAsync(this);

        //Connect to the TripService
        InitializeTripServiceConnection();
        BindTripService();

        //Define action upon retrieving route (Whatever was logged before opening the Live Map)
        mRouteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<Location> route = intent.getParcelableArrayListExtra(getString(R.string.BroadcastRouteLocationList));

                if (!route.isEmpty()) {
                    //Add all coordinates to the route
                    for (int i = route.size() - 1; i >= 0; i--) {
                        mRoute.add(0, new LatLng(route.get(i).getLatitude(), route.get(i).getLongitude()));
                    }

                    //Add all distance to the TripDistance
                    for (int i = 1; i < route.size() - 1; i++) {
                        mTripDistance += route.get(i).distanceTo(route.get(i-1));
                    }

                    //If start time of the trip has not been recorded yet, initialize the view for live time
                    if (mTripStartTime == null) {
                        mTripStartTime = route.get(0).getTime();
                        InitializeLiveTime();
                    }

                    //Update Map UI
                    UpdateRouteOnMap();
                }
            }
        };

        //Define action upon retrieving new locations
        mLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Location location = intent.getParcelableExtra(getString(R.string.BroadcastLiveGpsLocation));

                if (!mRoute.isEmpty()) {
                    //Add the new distance to TripDistance
                    float[] result = new float[1];
                    Location.distanceBetween(
                            location.getLatitude(),
                            location.getLongitude(),
                            mRoute.get(mRoute.size() - 1).latitude,
                            mRoute.get(mRoute.size() - 1).longitude,
                            result);
                    mTripDistance += result[0];
                }

                //Add new coordinate to the route
                mRoute.add(new LatLng(location.getLatitude(), location.getLongitude()));

                //If start time of the trip has not been recorded yet, initialize the view for live time
                if (mTripStartTime == null) {
                    mTripStartTime = location.getTime();
                    InitializeLiveTime();
                }

                //Update map UI
                UpdateRouteOnMap();
            }
        };

        //Start listening for positions with the receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(mRouteReceiver, new IntentFilter(getString(R.string.BroadcastRouteIntent)));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver, new IntentFilter(getString(R.string.BroadcastLiveGpsIntent)));

        //Set a listener for the Floating Action Button
        FloatingActionButton trackRouteButton = (FloatingActionButton) findViewById(R.id.TrackRouteButton);
        trackRouteButton.setOnClickListener(OnTrackRouteListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Initialize the map with a polyline
        mMap = googleMap;
        mRouteLine = mMap.addPolyline(mRouteOptions);
    }

    @Override
    public void onDestroy() {
        //Stop Live Time from updating
        mTripTimer.removeCallbacks(mTimerTask);
        super.onDestroy();
    }

    //endregion

    //region LISTENERS
    FloatingActionButton.OnClickListener OnTrackRouteListener = new FloatingActionButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            mKeepAnimating = true;
            UpdateRouteOnMap();
        }
    };

    //endregion

    private void UpdateRouteOnMap() {
        //If mRouteLine is null, the GoogleMap is not ready yet - Skip the update
        if (mRouteLine != null) {

            //Read newest position
            LatLng position = new LatLng(mRoute.get(mRoute.size() - 1).latitude, mRoute.get(mRoute.size() - 1).longitude);

            //Update the line
            mRouteLine.setPoints(mRoute);

            //Move camera to the new position
            if (mKeepAnimating) {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position), getResources().getInteger(R.integer.PositionInterval), mAnimationCallback);
            }

            //If marker exists, remove it
            if(mMarker != null) {
                mMarker.remove();
            }

            //Place new marker
            mMarkerOptions.position(position);
            mMarker = mMap.addMarker(mMarkerOptions);

            //Update distance view
            double distanceInKilometers = mTripDistance / 1000;
            TextView liveDistanceView = (TextView) findViewById(R.id.LiveDistanceView);
            liveDistanceView.setText(getString(R.string.LiveDistanceText, distanceInKilometers));
        }
    }

    private void InitializeLiveTime() {
        mTripTimer.postDelayed(mTimerTask, 1000);
    }

    //region TRIP SERVICE

    private void InitializeTripServiceConnection() {
        //Create a connection and a messenger for communication with the service
        //Enable/disable interaction with the service depending on connection status
        mTripServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMessenger = new Messenger(service);

                //As soon as the service is available, request the route that has been recorded so far
                UpdateRouteBroadcast();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMessenger = null;
            }
        };
    }

    private void BindTripService(){
        Intent intent = new Intent(this, TripService.class);
        bindService(intent, mTripServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void UpdateRouteBroadcast() {
        //Create message to TripService with intent to run case for UPDATE_ROUTE_BROADCAST
        Message message = Message.obtain(null, TripService.UPDATE_ROUTE_BROADCAST, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact TripService");
        }
    }

    //endregion
}
