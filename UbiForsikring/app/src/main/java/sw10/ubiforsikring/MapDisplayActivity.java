package sw10.ubiforsikring;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.FactObjects.Fact;

public class MapDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {
    Context mContext;
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
        mContext = this;
        mRoute = new ArrayList<>();

        //Get trip id for which data to display
        Intent intent = getIntent();
        mTripId = intent.getLongExtra(getString(R.string.MapDisplayIntent), -1);

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
        //Fetch route to display
        if (mRoute.isEmpty()) {
            RouteGetTask routeGetTask = new RouteGetTask(this);
            routeGetTask.execute(mTripId);
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

        if(!mRoute.isEmpty()) {
            PlaceMarkers();
            PlaceRoute();
        }

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

    private class RouteGetTask extends AsyncTask<Long, Void, Boolean> {
        final WeakReference<Context> mContextReference;

        public RouteGetTask(Context context) {
            mContextReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Long... tripId) {
            try {
                SharedPreferences preferences = getSharedPreferences(getString(R.string.UserPreferences), Context.MODE_PRIVATE);
                int userId = preferences.getInt(getString(R.string.StoredCarId), -1);

                List<Fact> facts = ServiceHelper.GetFactsForMap(userId, tripId[0]);
                for(Fact fact : facts) {
                    Location location = fact.SpatialTemporal.MPoint;
                    mRoute.add(new LatLng(location.getLatitude(), location.getLongitude()));
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mContextReference.get() != null) {
                if(!success) {
                    BuildAlertDialog().show();
                    return;
                }

                //Draw the route if Map is ready
                if (mMap != null) {
                    PlaceMarkers();
                    PlaceRoute();
                    CenterRoute();
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private AlertDialog BuildAlertDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.TripOverviewLoadErrorText))
                .setPositiveButton(getString(R.string.TripListRetryLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RouteGetTask routeGetTaskGetTask = new RouteGetTask(mContext);
                        routeGetTaskGetTask.execute(mTripId);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.TripOverviewErrorGoBack), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
    }
}
