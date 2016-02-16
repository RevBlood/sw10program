package sw10.lbforsikring.Objects.FactObjects;


import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Casper on 11-02-2016.
 */
public class Fact {
    public long EntryId;
    public long TripId;
    public int CarId;
    public FlagInformation Flag;
    public MeasureInformation Measure;
    public SpatialInformation Spatial;
    public TemporalInformation Temporal;

    public Fact(long EntryId, long TripId, int CarId, FlagInformation Flag, MeasureInformation Measure, SpatialInformation Spatial, TemporalInformation Temporal){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.CarId = CarId;
        this.Flag = Flag;
        this.Measure = Measure;
        this.Spatial = Spatial;
        this.Temporal = Temporal;
    }

    public Fact(JSONObject obj) {
        try {
            this.EntryId = obj.getLong("entryid");
            this.TripId = obj.getLong("tripid");
            this.CarId = obj.getInt("carid");

            this.Flag = new FlagInformation(obj.getJSONObject("flag"));
            //this.Measure = new MeasureInformation(obj.getJSONObject("measure"));
            //this.Spatial = new SpatialInformation(obj.getJSONObject("spatial"));
            //this.Temporal = new TemporalInformation(obj.getJSONObject("temporal"));
        }
        catch (Exception e){
            Log.e("Debug", "Fact - JSONObject:", e);
        }
    }

    public JSONObject serializeToJSON(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("entryid", EntryId);
            obj.put("tripid", TripId);
            obj.put("carid", CarId);

            obj.put("flag", Flag.serializeToJSON());
            obj.put("measure", Measure.serializeToJSON());
            obj.put("spatial", Spatial.serializeToJSON());
            obj.put("temporal", Temporal.serializeToJSON());
        } catch(Exception e) {
            Log.e("Debug", "Fact - Serialize:", e);
        }

        return obj;
    }




}
