package sw10.ubiforsikring.Objects.TripObjects;

import org.json.JSONObject;

public class Trip {
    public long TripId;
    public long PreviousTripId;
    public int CarId;
    public boolean IsActive = false;
    public boolean IsProcessing = false;

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
