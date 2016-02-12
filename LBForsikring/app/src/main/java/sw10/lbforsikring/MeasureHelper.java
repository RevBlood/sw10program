package sw10.lbforsikring;

import android.location.Location;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Casper on 11-02-2016.
 */
public final class MeasureHelper {

    public static double DistanceToLag(Location MPoint, Location PrevMPoint){
        return MPoint.distanceTo(PrevMPoint);
    }

    public static int SecondsToLag(Location MPoint, Location PrevMPoint){
        long time = MPoint.getTime();
        long prevTime = PrevMPoint.getTime();

        return (int)(time - prevTime);
    }

    public static double Speed(Location MPoint, Location PrevMPoint){
        //distance/time


        double distance = MPoint.distanceTo(PrevMPoint);

        long time = MPoint.getTime();
        long prevTime = PrevMPoint.getTime();
        int timeSpend = (int)(time - prevTime);

//      3.6 * m/s

        return (distance / timeSpend) * 3.6;
    }

    public static double Acceleration(){
        return 0.0;
    }

    public static int DBDate(long unixTime) {
        Date timestamp = new java.util.Date(unixTime);
        String dbDate = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        dbDate += calendar.get(Calendar.YEAR);
        dbDate += String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        dbDate += String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        Log.d("Debug", dbDate);
        return Integer.parseInt(dbDate);
    }

    public static int DBTime(long unixTime) {
        Date timestamp = new java.util.Date(unixTime);
        String dbTime = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        dbTime += String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        dbTime += String.format("%02d", calendar.get(Calendar.MINUTE));
        dbTime += String.format("%02d", calendar.get(Calendar.SECOND));
        Log.d("Debug", dbTime);
        return Integer.parseInt(dbTime);
    }
}
