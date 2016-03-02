package sw10.ubiforsikring.Helpers;

import android.location.Location;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import sw10.ubiforsikring.Objects.FactObjects.*;

/**
 * Created by Casper on 11-02-2016.
 */
public final class MeasureHelper {

    public static double DistanceToLag(Location MPoint, Location PrevMPoint){
        return MPoint.distanceTo(PrevMPoint);
    }

    public static int SecondsToLag(SpatialTemporalInformation CurrentTI, SpatialTemporalInformation PrevTI){
        return (int)(CurrentTI.MPoint.getTime() - PrevTI.MPoint.getTime()) / 1000;
    }

    public static double Speed(Location MPoint, Location PrevMPoint){
        //Speed = Distance / Time
        double distance = MPoint.distanceTo(PrevMPoint);

        long differenceInSeconds = (MPoint.getTime() - PrevMPoint.getTime()) / 1000;

        // Conversion from m/s to km/h
        //3.6 * m/s

        return (distance / differenceInSeconds) * 3.6;
    }

    public static double Acceleration(MeasureInformation CurrentMI, MeasureInformation PrevMI, SpatialTemporalInformation CurrentTI, SpatialTemporalInformation PrevTI){
        //Acceleration = Velocity change / Time
        double velocityChange = CurrentMI.Speed - PrevMI.Speed;
        long differenceInSeconds = (CurrentTI.MPoint.getTime() - PrevTI.MPoint.getTime()) / 1000;

        if(differenceInSeconds == 0){
            return 0;
        }

        return velocityChange / differenceInSeconds;
    }

    public static double Jerk(MeasureInformation CurrentMi, MeasureInformation PrevMI, SpatialTemporalInformation CurrentTI, SpatialTemporalInformation PrevTI){
        //Jerk = Acceleration change / Time
        double accelerationChange = CurrentMi.Acceleration - PrevMI.Acceleration;
        long differenceInSeconds = (CurrentTI.MPoint.getTime() - PrevTI.MPoint.getTime()) / 1000;

        if(differenceInSeconds == 0){
            return 0;
        }

        return accelerationChange / differenceInSeconds;
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
