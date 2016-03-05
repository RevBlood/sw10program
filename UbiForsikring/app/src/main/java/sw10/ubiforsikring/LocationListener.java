package sw10.ubiforsikring;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.MeasureHelper;

public class LocationListener implements com.google.android.gms.location.LocationListener {
    Context mContext;
    List<Location> mEntries = new ArrayList<>();
    Handler mMovementTimer;
    Runnable mTimerTask;
    Notification mMovementNotification;

    public LocationListener(Context context) {
        mContext = context;
        mMovementTimer = new Handler();

        //Define what to do when the mMovementTimer runs out
        mTimerTask = new Runnable() {
            @Override
            public void run() {
                Log.i("Debug", "No movement registered, issuing notification");
                IssueMovementNotification();
            }
        };
    }

    @Override
    public void onLocationChanged(Location location) {
        //While speed stays above 10km/h, keep resetting the movement timer
        //If speed is less than 10km/h, let the timer run out, issuing a notification
        //If a notification is issued, no more notifications will occur until speed has been above 10km/h again
        if(!mEntries.isEmpty() && MeasureHelper.Speed(location, mEntries.get(mEntries.size() - 1)) >= mContext.getResources().getInteger(R.integer.MovementMinSpeed)) {
            UpdateMovementTimer();
        }

        //Broadcast the new position so it can be retrieved elsewhere in the application
        BroadcastLiveGps(location);

        //Save the observed location
        Log.d("Debug", location.getLatitude() + ", " + location.getLongitude());
        mEntries.add(location);
    }

    private void BuildMovementNotification() {
        //Build notification, asking user to maybe stop the trip
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getString(R.string.MovementNotificationTitle))
                .setContentText(mContext.getString(R.string.MovementNotificationText))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        //Create intent to launch Main Menu when notification is pressed
        Intent intent = new Intent(mContext, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        mMovementNotification = notificationBuilder.build();
    }

    private void IssueMovementNotification(){
        if(mMovementNotification == null) {
            BuildMovementNotification();
        }

        //Pop the notification
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mMovementNotification);
    }

    private void BroadcastLiveGps(Location location) {
        Intent intent = new Intent(mContext.getString(R.string.BroadcastLiveGpsIntent));
        intent.putExtra(mContext.getString(R.string.BroadcastLiveGpsLocation), location);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void UpdateMovementTimer() {
        //If timer exists, cancel it. Then restart it
        mMovementTimer.removeCallbacks(mTimerTask);
        mMovementTimer.postDelayed(mTimerTask, mContext.getResources().getInteger(R.integer.MovementNotificationDelay));
    }

    public void DisableMovementTimer() {
        mMovementTimer.removeCallbacks(mTimerTask);
    }

    public void ClearMovementNotification() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    public List<Location> GetEntries() {
        return mEntries;
    }

    public void ClearEntries() {
        mEntries.clear();
    }
}
