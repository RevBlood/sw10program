package sw10.lbforsikring;

import android.location.Location;

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

}
