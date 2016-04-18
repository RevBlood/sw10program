package sw10.ubiforsikring.Objects.FactObjects;

import android.util.Log;

import org.json.JSONObject;

public class Fact {
    public long EntryId;
    public long TripId;
    public int CarId;
    public FlagInformation Flag;
    public MeasureInformation Measure;
    public SpatialTemporalInformation SpatialTemporal;

    public Fact(long TripId, int CarId, FlagInformation Flag, MeasureInformation Measure, SpatialTemporalInformation Spatial){
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
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("tripid", TripId);
            jsonObject.put("carid", CarId);

            jsonObject.put("temporal", SpatialTemporal.serializeTemporalToJSON());
            jsonObject.put("spatial", SpatialTemporal.serializeSpatialToJSON());

            jsonObject.put("flag", Flag.serializeToJSON());
            jsonObject.put("measure", Measure.serializeToJSON());
        } catch(Exception e) {
            Log.e("Debug", "Fact - Serialize:", e);
        }

        return jsonObject;
    }

    @Override public String toString() {
        String result = "";
        String NEW_LINE = System.getProperty("line.separator");

        result += this.getClass().getName() + " Object {" + NEW_LINE;
        result += " EntryId: " + EntryId + NEW_LINE;
        result += " TripId: " + TripId + NEW_LINE;
        result += " CarId: " + CarId + NEW_LINE;

        result += " FlagInformation: " + Flag.toString() + NEW_LINE;
        result += " MeasureInformation: " + Measure.toString() + NEW_LINE;
        result += " SpatialTemporalInformation: " + SpatialTemporal.toString();
        result += "}";

        return result;
    }


}
