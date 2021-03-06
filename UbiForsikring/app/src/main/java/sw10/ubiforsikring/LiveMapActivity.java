package sw10.ubiforsikring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LiveMapActivity extends AppCompatActivity implements OnMapReadyCallback {
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
    double mTripDistance;
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
        mRouteOptions.color(ContextCompat.getColor(this, R.color.black));
        mRouteOptions.width(getResources().getInteger(R.integer.LiveGpsRouteWidth));

        //Define how the position marker looks
        mCurrentMarkerOptions = new MarkerOptions();
        mCurrentMarkerOptions.icon(DrawableToBitmap(R.drawable.marker_end));
        mCurrentMarkerOptions.anchor(0.5f, 0.5f);

        //Define how the start marker looks
        mStartMarkerOptions = new MarkerOptions();
        mStartMarkerOptions.icon(DrawableToBitmap(R.drawable.marker_start));
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
        //Re-calculate distance before listening for new updates
        registerReceiver(mRouteReceiver, new IntentFilter(getString(R.string.BroadcastRouteIntent)));

        //Connect to the TripService
        InitializeTripServiceConnection();
        BindTripService();

        super.onResume();
    }

    @Override
    public void onPause() {
        //Stop updating the trip timer
        mTripTimer.removeCallbacks(mTimerTask);

        //If activity is paused, stop listening for new locations
        unregisterReceiver(mLocationReceiver);

        //Disconnect from the TripService
        unbindService(mTripServiceConnection);

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
            //Unregister the receiver - We only need the route once
            unregisterReceiver(mRouteReceiver);

            //Having received a route, clear any earlier data, if any
            mRoute.clear();
            mStartMarker = null;
            mCurrentMarker = null;
            mTripStartTime = null;
            mTripDistance = 0;

            //Redraw polyline, if GoogleMap is ready
            if (mMap != null) {
                mMap.clear();
                mRouteLine = mMap.addPolyline(mRouteOptions);
            }

            // Read the route from SharedPreferences
            List<LatLng> route = new ArrayList<>();
            SharedPreferences preferences = getSharedPreferences(getString(R.string.SW10Preferences), Context.MODE_MULTI_PROCESS);
            Set<String> values = preferences.getStringSet(getString(R.string.StoredRoute), new HashSet<String>());

            for (String value : values) {
                String[] latLng = value.split(";");
                route.add(new LatLng((Double.parseDouble(latLng[0])), Double.parseDouble(latLng[1])));
            }

            if(!route.isEmpty()) {
                //Add all coordinates to the route
                for (int i = route.size() - 1; i >= 0; i--) {
                    mRoute.add(route.get(i));
                }

                //Add all distance to the TripDistance
                for (int i = 1; i < route.size() - 1; i++) {
                    mTripDistance += DistanceBetweenLatLng(route.get(i), route.get(i - 1));
                }

                //Initialize the view for live time
                mTripStartTime = preferences.getLong(getString(R.string.StoredRouteStart), -1);
                InitializeLiveTime();

                //Update Map UI
                UpdateRouteOnMap();
            }

            //Listen for position updates
            mLocationReceiver = new PositionReceiver();
            registerReceiver(mLocationReceiver, new IntentFilter(getString(R.string.BroadcastLiveGpsIntent)));
        }
    }

    private class PositionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(getString(R.string.BroadcastLiveGpsLocation));
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            if (!mRoute.isEmpty()) {
                mTripDistance += DistanceBetweenLatLng(position, mRoute.get(mRoute.size() - 1));
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
                MessageTripService(TripService.UPDATE_ROUTE_BROADCAST);
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

    private boolean MessageTripService(int messageId) {
        //Create message to TripService with intent to run case for BEGIN_TRIP
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
                mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
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

    private BitmapDescriptor DrawableToBitmap(int drawable) {
        Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable shape = ContextCompat.getDrawable(this, drawable);
        shape.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        shape.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //endregion
}