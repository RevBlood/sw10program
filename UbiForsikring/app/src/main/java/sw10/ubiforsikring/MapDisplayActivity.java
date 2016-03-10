package sw10.ubiforsikring;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapDisplayActivity extends FragmentActivity implements OnMapReadyCallback {
    long mTripId;

    //Map
    GoogleMap mMap;
    Polyline mRouteLine;
    PolylineOptions mRouteOptions;
    MarkerOptions mStartMarkerOptions;
    MarkerOptions mEndMarkerOptions;
    Marker mStartMarker;
    Marker mEndMarker;

    //region ACTIVITY EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);

        //Get trip id for which data to display
        Intent intent = getIntent();
        mTripId = intent.getLongExtra(getString(R.string.TripIdIntentName), -1);

        //Define how the route looks on the map
        mRouteOptions = new PolylineOptions();
        mRouteOptions.color(ContextCompat.getColor(this, R.color.colorPrimary));
        mRouteOptions.width(getResources().getInteger(R.integer.LiveGpsRouteWidth));

        //Define how the start marker looks
        mStartMarkerOptions = new MarkerOptions();
        mStartMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mStartMarkerOptions.anchor(0.5f, 0.5f);

        //Define how the end marker looks
        mEndMarkerOptions = new MarkerOptions();
        mEndMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mEndMarkerOptions.anchor(0.5f, 0.5f);

        //Get the map ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MapDisplay);
        mapFragment.getMapAsync(this);

        //Set a listener for the Floating Action Button
        FloatingActionButton centerRouteButton = (FloatingActionButton) findViewById(R.id.CenterRouteButton);
        centerRouteButton.setOnClickListener(OnCenterRouteListener);
    }

    @Override
    public void onResume() {
        //Redraw polyline, if GoogleMap is ready
        if (mMap != null) {
            mRouteLine = mMap.addPolyline(mRouteOptions);
        }

        //TODO: Fetch route here

        super.onResume();
    }

    @Override
    public void onPause() {
        //Clear data from map
        mMap.clear();
        mStartMarker = null;
        mEndMarker = null;

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

    FloatingActionButton.OnClickListener OnCenterRouteListener = new FloatingActionButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            CenterRoute();
        }
    };

    //endregion

    private void CenterRoute() {
        //TODO: Zoom to route
    }
}
