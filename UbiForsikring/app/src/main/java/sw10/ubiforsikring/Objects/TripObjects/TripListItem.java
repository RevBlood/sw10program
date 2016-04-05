package sw10.ubiforsikring.Objects.TripObjects;

import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

import sw10.ubiforsikring.Helpers.DateObjectHelper;

public class TripListItem {
    public long TripId;
    public Date TripStart;
    public Date TripEnd;
    public double MetersDriven;

    public double Price;
    public double OptimalScore;
    public double TripScore;

    public long PreviousTripId;
    public int CarId;
    public boolean IsActive = false;
    public boolean IsProcessing = false;

    public TripListItem(boolean isActive, boolean isProcessing) {
        IsActive = isActive;
        IsProcessing = isProcessing;
    }

    public TripListItem(JSONObject obj) {
        try {
            this.TripId = obj.getLong("tripid");
            this.TripStart = DateObjectHelper.CreateDateObject(new JSONObject(obj.getString("starttemporal")).getString("timestamp"));
            this.TripEnd = DateObjectHelper.CreateDateObject(new JSONObject(obj.getString("endtemporal")).getString("timestamp"));
            this.MetersDriven = obj.getDouble("metersdriven");
            this.Price = obj.getDouble("price");
            this.OptimalScore = obj.getDouble("optimalscore");
            this.TripScore = obj.getDouble("tripscore");
            //this.PreviousTripId = obj.getLong("prevtripid");
            //this.CarId = obj.getInt("carid");
        }
        catch (Exception e){
            Log.e("Debug", "Trip JsonObject Constructor: ", e);
        }
    }


    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append(this.getClass().getName() + " Object {" + NEW_LINE);
        result.append(" TripId: " + TripId + NEW_LINE);
        result.append(" TripStart: " + TripStart.toString() + NEW_LINE );
        result.append(" TripEnd: " + TripEnd.toString() + NEW_LINE );
        result.append(" MetersDriven: " + MetersDriven + NEW_LINE );
        result.append(" Price: " + Price + NEW_LINE );
        result.append(" OptimalScore: " + OptimalScore + NEW_LINE );
        result.append(" TripScore: " + TripScore );




        //result.append(" CarId: " + CarId + NEW_LINE );


        return result.toString();
    }

}
