package sw10.ubiforsikring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapDisplayActivity extends FragmentActivity implements OnMapReadyCallback {
    long mTripId;

    //Map
    GoogleMap mMap;
    List<LatLng> mRoute;
    Polyline mRouteLine;
    PolylineOptions mRouteOptions;
    MarkerOptions mStartMarkerOptions;
    MarkerOptions mEndMarkerOptions;

    //region ACTIVITY EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);
        mRoute = new ArrayList<>();

        //Get trip id for which data to display
        Intent intent = getIntent();
        mTripId = intent.getLongExtra(getString(R.string.TripIdIntentName), -1);

        //Define how the route looks on the map
        mRouteOptions = new PolylineOptions();
        mRouteOptions.color(ContextCompat.getColor(this, R.color.black));
        mRouteOptions.width(getResources().getInteger(R.integer.LiveGpsRouteWidth));

        //Define how the start marker looks
        mStartMarkerOptions = new MarkerOptions();
        mStartMarkerOptions.icon(DrawableToBitmap(R.drawable.marker_start));
        mStartMarkerOptions.anchor(0.5f, 0.5f);

        //Define how the end marker looks
        mEndMarkerOptions = new MarkerOptions();
        mEndMarkerOptions.icon(DrawableToBitmap(R.drawable.marker_end));
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
        //TODO: Fetch the actual route
        mRoute.add(new LatLng(6.8, 4.4));
        mRoute.add(new LatLng(6.7, 4.3));

        //Draw on map, if ready
        if (mMap != null) {
            PlaceMarkers();
            PlaceRoute();
            CenterRoute();
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        //Clear data from map
        mMap.clear();
        mRoute.clear();

        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Initialize the map with a polyline
        mMap = googleMap;
        PlaceMarkers();
        PlaceRoute();
        mMap.setOnMapLoadedCallback(MapLoadedCallback);
    }

    //endregion

    //region LISTENERS

    FloatingActionButton.OnClickListener OnCenterRouteListener = new FloatingActionButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            CenterRoute();
        }
    };

    GoogleMap.OnMapLoadedCallback MapLoadedCallback = new GoogleMap.OnMapLoadedCallback() {
        @Override
        public void onMapLoaded() {
            CenterRoute();
        }
    };

    //endregion

    private void CenterRoute() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng position : mRoute) {
            builder.include(position);
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, getResources().getInteger(R.integer.ZoomToRoutePadding));
        mMap.animateCamera(cu);
    }

    private void PlaceMarkers() {
        mStartMarkerOptions.position(mRoute.get(0));
        mEndMarkerOptions.position(mRoute.get(mRoute.size() - 1));
        mMap.addMarker(mStartMarkerOptions);
        mMap.addMarker(mEndMarkerOptions);
    }

    private void PlaceRoute() {
        mRouteLine = mMap.addPolyline(mRouteOptions);
        mRouteLine.setPoints(mRoute);
    }

    private BitmapDescriptor DrawableToBitmap(int drawable) {
        Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable shape = ContextCompat.getDrawable(this, drawable);
        shape.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        shape.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
