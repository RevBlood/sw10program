package sw10.lbforsikring.Objects.TripObjects;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Casper on 15-02-2016.
 */


public class Trip {
    public long TripId;
    public long PreviousTripId;
    public int CarId;

    public Trip(){

    }

    public Trip(JSONObject obj) {
        try {
            this.TripId = obj.getLong("tripid");
            this.PreviousTripId = obj.getLong("prevtripid");
            this.CarId = obj.getInt("carid");
        }
        catch (Exception e){

        }
    }
}
