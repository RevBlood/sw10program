package sw10.lbforsikring;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by treel on 09-02-2016.
 */
    public class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);
            Log.e("App", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
}
