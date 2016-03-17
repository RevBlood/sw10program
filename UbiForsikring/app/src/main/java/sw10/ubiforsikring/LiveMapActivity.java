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
    Context mContext;

    //TripService communication
    ServiceConnection mTripServiceConnection;
    Messenger mMessenger;
    BroadcastReceiver mRouteReceiver;
    BroadcastReceiver mLocationReceiver;

    //Map
    GoogleMap mMap;
    List<LatLng> mRoute;
    Polyline mRouteLine;
    PolylineOptions mRouteOptions;
    MarkerOptions mStartMarkerOptions;
    MarkerOptions mCurrentMarkerOptions;
    Marker mStartMarker;
    Marker mCurrentMarker;
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
        mContext = this;
        mRouteReceiver = new RouteReceiver();

        //Initialize variables for later use
        mSdf = new SimpleDateFormat(getString(R.string.LiveTimeTextFormat));
        mTripTimer = new Handler();
        mRoute = new ArrayList<>();
        mTimerTask = new TimerTask();
        mAnimationCallback = new AnimationCallback();

        //Define how the route looks on the map
        mRouteOptions = new PolylineOptions();
        mRouteOptions.color(ContextCompat.getColor(this, R.color.colorPrimary));
        mRouteOptions.width(getResources().getInteger(R.integer.LiveGpsRouteWidth));

        //Define how the position marker looks
        mCurrentMarkerOptions = new MarkerOptions();
        mCurrentMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mCurrentMarkerOptions.anchor(0.5f, 0.5f);

        //Define how the start marker looks
        mStartMarkerOptions = new MarkerOptions();
        mStartMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mStartMarkerOptions.anchor(0.5f, 0.5f);

        //Get the map ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.LiveMap);
        mapFragment.getMapAsync(this);

        //Set a listener for the Floating Action Button
        FloatingActionButton trackRouteButton = (FloatingActionButton) findViewById(R.id.TrackRouteButton);
        trackRouteButton.setOnClickListener(OnTrackRouteListener);
    }

    @Override
    public void onResume() {
        //Whenever activity is resumed re-calculate distance before listening for new updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mRouteReceiver, new IntentFilter(getString(R.string.BroadcastRouteIntent)));

        //Connect to the TripService
        InitializeTripServiceConnection();
        BindTripService();

        //Redraw polyline, if GoogleMap is ready
        if (mMap != null) {
            mRouteLine = mMap.addPolyline(mRouteOptions);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        //If activity is paused, stop listening for new locations
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);

        //Disconnect from the TripService
        unbindService(mTripServiceConnection);

        //Clear data
        mMap.clear();
        mRoute.clear();
        mStartMarker = null;
        mCurrentMarker = null;
        mTripDistance = 0;

        //Stop Live Time from updating until activity is resumed
        mTripTimer.removeCallbacks(mTimerTask);

        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Initialize the map with a polyline
        mMap = googleMap;
        mRouteLine = mMap.addPolyline(mRouteOptions);
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

    //region INCOMING EVENTS

    private class RouteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<Location> route = intent.getParcelableArrayListExtra(getString(R.string.BroadcastRouteLocationList));

            if(!route.isEmpty()) {
                //Add all coordinates to the route
                for (int i = route.size() - 1; i >= 0; i--) {
                    mRoute.add(0, new LatLng(route.get(i).getLatitude(), route.get(i).getLongitude()));
                }

                //Add all distance to the TripDistance
                double test = mTripDistance;
                for (int i = 1; i < route.size() - 1; i++) {
                    mTripDistance += route.get(i).distanceTo(route.get(i - 1));
                }
                Log.d("Debug", "Route: " + Double.toString(mTripDistance - test));

                //If start time of the trip has not been recorded yet, initialize the view for live time
                if (mTripStartTime == null) {
                    mTripStartTime = route.get(0).getTime();
                    InitializeLiveTime();
                }

                //Update Map UI
                UpdateRouteOnMap();
            }

            //Unregister the receiver - We only need the route once
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mRouteReceiver);

            //Listen for position updates
            mLocationReceiver = new PositionReceiver();
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocationReceiver, new IntentFilter(getString(R.string.BroadcastLiveGpsIntent)));
        }
    }

    private class PositionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(getString(R.string.BroadcastLiveGpsLocation));
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            if (!mRoute.isEmpty()) {
                mTripDistance += DistanceBetweenLatLng(position, mRoute.get(mRoute.size() - 1));

                Log.d("Debug", "Position: " + Double.toString(DistanceBetweenLatLng(position, mRoute.get(mRoute.size() - 1))));
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
    }

    //endregion

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

    //region OTHER

    private class TimerTask implements Runnable {
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
    }

    private class AnimationCallback implements GoogleMap.CancelableCallback {
        @Override
        public void onFinish() {

        }
        @Override
        public void onCancel() {
            mKeepAnimating = false;
        }
    }

    private void InitializeLiveTime() {
        mTripTimer.postDelayed(mTimerTask, 1000);
    }

    private void UpdateRouteOnMap() {
        //If mRouteLine is null, the GoogleMap is not ready yet - Skip the update
        if (mRouteLine != null) {

            //If start marker does not exist, place it
            if(mStartMarker == null) {
                mStartMarkerOptions.position(mRoute.get(0));
                mStartMarker = mMap.addMarker(mStartMarkerOptions);
            }

            //Read newest position
            LatLng position = new LatLng(mRoute.get(mRoute.size() - 1).latitude, mRoute.get(mRoute.size() - 1).longitude);

            //Update the line
            mRouteLine.setPoints(mRoute);

            //Move camera to the new position
            if (mKeepAnimating) {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position), getResources().getInteger(R.integer.AnimationSpeed), mAnimationCallback);
            }

            //If marker exists, remove it
            if(mCurrentMarker != null) {
                mCurrentMarker.remove();
            }

            //Place new marker
            mCurrentMarkerOptions.position(position);
            mCurrentMarker = mMap.addMarker(mCurrentMarkerOptions);

            //Update distance view
            TextView liveDistanceView = (TextView) findViewById(R.id.LiveDistanceView);
            liveDistanceView.setText(getString(R.string.LiveDistanceText, mTripDistance /1000));
        }
    }

    private double DistanceBetweenLatLng(LatLng first, LatLng second) {
        float[] result = new float[1];
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, result);
        return result[0];
    }

    //endregion
}