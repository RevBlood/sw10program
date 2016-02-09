package sw10.lbforsikring;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationService extends Service {
    private LocationManager mLocationManager;
    private LocationListener mGPSListener;
    private LocationListener mNetworkListener;

    @Override
    public void onCreate() {
        mGPSListener = new LocationListener(LocationManager.GPS_PROVIDER);
        mNetworkListener = new LocationListener(LocationManager.NETWORK_PROVIDER);

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Notify user of the missing permission
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                R.integer.Interval,
                                                R.integer.Distance,
                                                mGPSListener);
        Log.e("App", "Requesting GPS");

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                                R.integer.Interval,
                                                R.integer.Distance,
                                                mNetworkListener);

        Log.e("App", "Requesting Network");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}