package sw10.lbforsikring;

import android.location.Location;
import android.util.Log;

/**
 * Created by treel on 09-02-2016.
 */
    public class LocationListener implements com.google.android.gms.location.LocationListener {
        Location mLastLocation;

        public LocationListener() {
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.e("App", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
        }
}
