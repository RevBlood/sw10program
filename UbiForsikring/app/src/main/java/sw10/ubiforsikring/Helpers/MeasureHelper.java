package sw10.ubiforsikring.Helpers;

import android.location.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import sw10.ubiforsikring.Objects.FactObjects.*;

public final class MeasureHelper {
    public static void CalculateMeasures(ArrayList<Fact> facts) {
        if (facts.size() < 2) {
            return;
        }

        //First case - Set speed equal to second fact and set flags to false. This is essentially a freebie
        double speed2ndFact = Speed(facts.get(1).SpatialTemporal.MPoint, facts.get(0).SpatialTemporal.MPoint);
        facts.get(0).Measure = new MeasureInformation(speed2ndFact, 0, 0);
        facts.get(0).Flag = new FlagInformation(false, false, false, false);

        for(int i = 1; i < facts.size(); i++) {
            //Spatial
            facts.get(i).SpatialTemporal.DistanceToLag = DistanceToLag(facts.get(i).SpatialTemporal, facts.get(i-1).SpatialTemporal);

            //Temporal
            facts.get(i).SpatialTemporal.SecondsToLag = SecondsToLag(facts.get(i).SpatialTemporal, facts.get(i-1).SpatialTemporal);

            //MeasureInformation
            double speed = Speed(facts.get(i).SpatialTemporal.MPoint, facts.get(i - 1).SpatialTemporal.MPoint);
            facts.get(i).Measure = new MeasureInformation(speed, 0, 0);
            facts.get(i).Measure.Acceleration = Acceleration(facts.get(i).Measure, facts.get(i - 1).Measure, facts.get(i).SpatialTemporal, facts.get(i - 1).SpatialTemporal);
            facts.get(i).Measure.Jerk = Jerk(facts.get(i).Measure, facts.get(i - 1).Measure, facts.get(i).SpatialTemporal, facts.get(i - 1).SpatialTemporal);

            //FlagInformation
            Boolean accelerating = Accelerating(facts.get(i).Measure);
            Boolean braking = Braking(facts.get(i).Measure);
            Boolean jerking = Jerking(facts.get(i).Measure);
            facts.get(i).Flag = new FlagInformation(false, accelerating, braking, jerking);
        }

    }

    public static double DistanceToLag(Location MPoint, Location PrevMPoint){
        return MPoint.distanceTo(PrevMPoint);
    }

    public static double DistanceToLag(SpatialTemporalInformation CurrentTI, SpatialTemporalInformation PrevTI){
        return CurrentTI.MPoint.distanceTo(PrevTI.MPoint);
    }

    public static int SecondsToLag(SpatialTemporalInformation CurrentTI, SpatialTemporalInformation PrevTI){
        return (int)(CurrentTI.MPoint.getTime() - PrevTI.MPoint.getTime()) / 1000;
    }

    public static double Speed(Location MPoint, Location PrevMPoint){
        //Speed = Distance / Time
        double distance = MPoint.distanceTo(PrevMPoint);

        long differenceInSeconds = (MPoint.getTime() - PrevMPoint.getTime()) / 1000;

        if(differenceInSeconds == 0) {
            return 0;
        } else {
            // Conversion from m/s to km/h = (m/s) * 3.6
            return (distance / differenceInSeconds) * 3.6;
        }
    }

    public static double Acceleration(MeasureInformation CurrentMI, MeasureInformation PrevMI, SpatialTemporalInformation CurrentTI, SpatialTemporalInformation PrevTI){
        //Acceleration = Velocity change / Time
        double velocityChange = CurrentMI.Speed - PrevMI.Speed;
        long differenceInSeconds = (CurrentTI.MPoint.getTime() - PrevTI.MPoint.getTime()) / 1000;

        if(differenceInSeconds == 0){
            return 0;
        } else {
            return velocityChange / differenceInSeconds;
        }
    }

    public static double Jerk(MeasureInformation CurrentMi, MeasureInformation PrevMI, SpatialTemporalInformation CurrentTI, SpatialTemporalInformation PrevTI){
        //Jerk = Acceleration change / Time
        double accelerationChange = CurrentMi.Acceleration - PrevMI.Acceleration;
        long differenceInSeconds = (CurrentTI.MPoint.getTime() - PrevTI.MPoint.getTime()) / 1000;

        if(differenceInSeconds == 0){
            return 0;
        } else {
            return accelerationChange / differenceInSeconds;
        }
    }

    public static int DBDate(long unixTime) {
        Date timestamp = new java.util.Date(unixTime);
        String dbDate = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        dbDate += calendar.get(Calendar.YEAR);
        dbDate += String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        dbDate += String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
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
        return Integer.parseInt(dbTime);
    }

    private static Boolean Accelerating(MeasureInformation MI) {
        return MI.Acceleration >= 5;
    }

    private static Boolean Jerking(MeasureInformation MI) {
        return Math.abs(MI.Jerk) >= 5;
    }

    private static Boolean Braking(MeasureInformation MI) {
        return MI.Acceleration <= -5;
    }
}
