package sw10.ubiforsikring.Objects.FactObjects;


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
    public SpatialTemporalInformation SpatialTemporal;

    public Fact(long EntryId, long TripId, int CarId, FlagInformation Flag, MeasureInformation Measure, SpatialTemporalInformation Spatial){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.CarId = CarId;
        this.Flag = Flag;
        this.Measure = Measure;
        this.SpatialTemporal = Spatial;
    }

    public Fact(int carId, SpatialTemporalInformation spatialTemporalInformation) {
        CarId = carId;
        SpatialTemporal = spatialTemporalInformation;
    }

    public Fact(JSONObject obj) {
        try {
            this.EntryId = obj.getLong("entryid");
            this.TripId = obj.getLong("tripid");
            this.CarId = obj.getInt("carid");

            this.Flag = new FlagInformation(obj.getJSONObject("flag"));
            this.Measure = new MeasureInformation(obj.getJSONObject("measure"));
            this.SpatialTemporal = new SpatialTemporalInformation(obj.getJSONObject("spatial"), obj.getJSONObject("temporal"));
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

            obj.put("temporal", SpatialTemporal.serializeTemporalToJSON());
            obj.put("spatial", SpatialTemporal.serializeSpatialToJSON());

            //.put("flag", Flag.serializeToJSON());
            //obj.put("measure", Measure.serializeToJSON());


        } catch(Exception e) {
            Log.e("Debug", "Fact - Serialize:", e);
        }

        return obj;
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append(this.getClass().getName() + " Object {" + NEW_LINE);
        result.append(" EntryId: " + EntryId + NEW_LINE);
        result.append(" TripId: " + TripId + NEW_LINE);
        result.append(" CarId: " + CarId + NEW_LINE );

        result.append(" FlagInformation: " + Flag.toString() + NEW_LINE);
        result.append(" MeasureInformation: " + Measure.toString() + NEW_LINE);
        result.append(" SpatialTemporalInformation: " + SpatialTemporal.toString());
        result.append("}");

        return result.toString();
    }


}
