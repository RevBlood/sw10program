package sw10.ubiforsikring.Objects.TripObjects;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import sw10.ubiforsikring.Helpers.DateObjectHelper;

public class Trip implements Serializable {
    public long TripId;
    public long LocalTripId;
    public Date TripStart;
    public Date TripEnd;
    public double MetersDriven;

    //public double Price;
    public double OptimalScore;
    public double TripScore;

    public double MetersSped;
    public int AccelerationCount;
    public int BrakeCount;
    public int JerkCount;
    public int RoadtypeMajority;
    public int TimePeriodMajority;

    public double SpeedingScore;
    public double AccelerationScore;
    public double Brakescore;
    public double Jerkscore;
    public double RoadtypeScore;
    public double CriticalTimeScore;

    public Trip(JSONObject obj) {
        try {
            this.TripId = obj.getLong("tripid");
            this.LocalTripId = obj.getLong("localtripid");
            this.TripStart = DateObjectHelper.CreateDateObject(new JSONObject(obj.getString("starttemporal")).getString("timestamp"));
            this.TripEnd = DateObjectHelper.CreateDateObject(new JSONObject(obj.getString("endtemporal")).getString("timestamp"));
            this.MetersDriven = obj.getDouble("metersdriven");
            //this.Price = obj.getDouble("price");
            this.OptimalScore = obj.getDouble("optimalscore");
            this.TripScore = obj.getDouble("tripscore");
            //this.PreviousTripId = obj.getLong("prevtripid");
            //this.CarId = obj.getInt("carid");
            this.RoadtypeScore = obj.getDouble("roadtypescore");
            this.CriticalTimeScore = obj.getDouble("criticaltimescore");
            this.SpeedingScore = obj.getDouble("speedingscore");
            this.AccelerationScore = obj.getDouble("accelerationscore");
            this.Brakescore = obj.getDouble("brakescore");
            this.Jerkscore = obj.getDouble("jerkscore");
            this.MetersSped = obj.getInt("meterssped");
            this.AccelerationCount = obj.getInt("accelerationcount");
            this.BrakeCount = obj.getInt("brakecount");
            this.JerkCount = obj.getInt("jerkcount");

        } catch (Exception e){
        }
    }

    @Override public String toString() {
        String result = "";
        String NEW_LINE = System.getProperty("line.separator");

        result += this.getClass().getName() + " Object {" + NEW_LINE;
        result += " TripId: " + TripId + NEW_LINE;
        result += " TripStart: " + TripStart.toString() + NEW_LINE;
        result += " TripEnd: " + TripEnd.toString() + NEW_LINE;
        result += " MetersDriven: " + MetersDriven + NEW_LINE;
        //result += " Price: " + Price + NEW_LINE;
        result += " OptimalScore: " + OptimalScore + NEW_LINE;
        result += " TripScore: " + TripScore + NEW_LINE;
        result += " RoadtypeScore: " + RoadtypeScore + NEW_LINE;
        result += " CriticalTimeScore: " + CriticalTimeScore + NEW_LINE;
        result += " SpeedingScore: " + SpeedingScore + NEW_LINE;
        result += " AccelerationScore: " + AccelerationScore + NEW_LINE;
        result += " Brakescore: " + Brakescore + NEW_LINE;
        result += " Jerkscore: " + Jerkscore;
        //result += " CarId: " + CarId + NEW_LINE;

        return result;
    }

}
