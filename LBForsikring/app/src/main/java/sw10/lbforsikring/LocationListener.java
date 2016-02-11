package sw10.lbforsikring;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by treel on 09-02-2016.
 */
public class LocationListener implements com.google.android.gms.location.LocationListener {
    Context mContext;
    Location mPreviousLocation;
    Handler mMovementTimer;
    Runnable mTimerTask;

    public LocationListener(Context context) {
        mContext = context;

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
        if(GetSpeed(location) >= R.integer.MovementMinSpeed) {
            UpdateMovementTimer();
        }

        //Save the observed location as the previous location
        Log.d("Debug", location.getLatitude() + ", " + location.getLongitude());
        mPreviousLocation = location;
    }

    private double GetSpeed(Location location) {
        //If not previous location exists, there is no speed - return 0
        if (mPreviousLocation != null) {
            //Get the difference in time and distance and calculate speed
            double timeDifference = location.getTime() - mPreviousLocation.getTime() / 1000;
            float distanceDifference = mPreviousLocation.distanceTo(location);

            //Return calculated speed
            return (distanceDifference / timeDifference) * 3.6;
        } else {
            return 0;
        }
    }

    public void UpdateMovementTimer() {
        //If timer exists, cancel it. Then restart it
        mMovementTimer.removeCallbacks(mTimerTask);
        mMovementTimer.postDelayed(mTimerTask, R.integer.NotificationDelay);
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
}
