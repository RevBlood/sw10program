package sw10.ubiforsikring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
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

    //region ACTIVITY EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);
        //Initialize variables for later use
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

        //Define how the route looks
        mRouteOptions = new PolylineOptions();
        mRouteOptions.color(ContextCompat.getColor(this, R.color.colorPrimary));
        mRouteOptions.width(getResources().getInteger(R.integer.LiveGpsRouteWidth));

        //Define how the marker looks
        mMarkerOptions = new MarkerOptions();
        mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mMarkerOptions.anchor(0.5f, 0.5f);

        //Get the map ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.LiveMap);
        mapFragment.getMapAsync(this);

        //Connect to the TripService
        InitializeTripServiceConnection();
        BindTripService();

        //Define action for retrieving route (Whatever was logged before opening the Live Map)
        mRouteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<Location> route = intent.getParcelableArrayListExtra(getString(R.string.BroadcastRouteLocationList));

                if (!route.isEmpty()) {
                    for (int i = route.size() - 1; i >= 0; i--) {
                        mRoute.add(0, new LatLng(route.get(i).getLatitude(), route.get(i).getLongitude()));
                    }

                    UpdateRouteOnMap();
                }
            }
        };

        //Define action for retrieving new locations
        mLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location location = intent.getParcelableExtra(getString(R.string.BroadcastLiveGpsLocation));
                mRoute.add(new LatLng(location.getLatitude(), location.getLongitude()));
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
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
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
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position), getResources().getInteger(R.integer.PositionInterval), mAnimationCallback);
            }

            //If marker exists, remove it
            if(mMarker != null) {
                mMarker.remove();
            }

            //Place new marker
            mMarkerOptions.position(position);
            mMarker = mMap.addMarker(mMarkerOptions);
        }
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
