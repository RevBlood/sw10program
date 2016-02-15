package sw10.lbforsikring;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by treel on 09-02-2016.
 */
public class LocationListener implements com.google.android.gms.location.LocationListener {
    Context mContext;
    Location mPreviousLocation;
    Handler mMovementTimer;
    Runnable mTimerTask;
    LBDatabaseHelper mDBHelper;
    dbWriteQueries mDBWriter;

    public LocationListener(Context context) {
        mContext = context;

        //Instantiate the database
        mDBHelper = new LBDatabaseHelper(mContext);
        mDBWriter = new dbWriteQueries(mDBHelper);

        //Define what to do when the mMovementTimer runs out
        mTimerTask = new Runnable() {
            @Override
            public void run() {
                Log.i("Debug", "No movement registered, issuing notification");
                IssueNotification();
            }
        };

        //Start the timer
        mMovementTimer = new Handler();
        UpdateMovementTimer();
    }

    @Override
    public void onLocationChanged(Location location) {
        //While speed is above 10km/h, keep resetting the movement timer
        //If speed is less than 10km/h, let the timer run out, issuing a notification
        //If a notification is issued, no more notifications will occur until speed has been above 10km/h again
        if(mPreviousLocation != null && MeasureHelper.Speed(location, mPreviousLocation) >= mContext.getResources().getInteger(R.integer.MovementMinSpeed)) {
            UpdateMovementTimer();
        }

        //Broadcast the new position so it can be retrieved elsewhere in the application
        BroadcastLocation(location);

        //Save the observed location as the previous location
        Log.d("Debug", location.getLatitude() + ", " + location.getLongitude());
        mPreviousLocation = location;

        //Save the observed location in DB
        long rowID = mDBWriter.InsertLocationIntoGPS(location);
        Log.d("Debug", Long.toString(rowID));
    }

    public void UpdateMovementTimer() {
        //If timer exists, cancel it. Then restart it
        mMovementTimer.removeCallbacks(mTimerTask);
        mMovementTimer.postDelayed(mTimerTask, mContext.getResources().getInteger(R.integer.NotificationDelay));
    }

    public void IssueNotification(){
        //Build notification, asking user to maybe stop the trip
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getString(R.string.NotificationTitle))
                .setContentText(mContext.getString(R.string.NotificationText))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        //Create intent to launch MainActivity when notification is pressed
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        //Pop the notification
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void BroadcastLocation(Location location) {
        Intent intent = new Intent(mContext.getString(R.string.BroadcastIntentName));
        Bundle bundle = new Bundle();
        bundle.putParcelable(mContext.getString(R.string.BroadcastParcelableLocationName), location);
        intent.putExtra(mContext.getString(R.string.BroadcastIntentBundleName), bundle);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
