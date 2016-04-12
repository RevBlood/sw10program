package sw10.ubiforsikring.Objects.FactObjects;

import android.util.Log;

import org.json.JSONObject;

public class MeasureInformation {
    public long EntryId;
    public long TripId;
    public double Speed;
    public double Acceleration;
    public double Jerk;

    public MeasureInformation(long EntryId, long TripId, double Speed, double Acceleration, double Jerk){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.Speed = Speed;
        this.Acceleration = Acceleration;
        this.Jerk = Jerk;
    }

    public MeasureInformation(double Speed, double Acceleration, double Jerk){
        this.Speed = Speed;
        this.Acceleration = Acceleration;
        this.Jerk = Jerk;
    }

    public MeasureInformation(JSONObject jsonObject) {
        try {
            this.Speed = jsonObject.isNull("speed") ? 0 : jsonObject.getDouble("speed");
            this.Acceleration = jsonObject.isNull("acceleration") ? 0 : jsonObject.getDouble("acceleration");
            this.Jerk = jsonObject.isNull("jerk") ? 0 : jsonObject.getDouble("jerk");
        } catch (Exception e){
            Log.e("Debug", "MeasureInformation - JSONObject:", e);
        }
    }

    public JSONObject serializeToJSON(){
        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put("speed", Speed);
            jsonObject.put("acceleration", Acceleration);
            jsonObject.put("jerk", Jerk);
        } catch(Exception e) {
            Log.e("Debug", "MeasureInformation - Serialize:", e);
        }

        return jsonObject;
    }

    @Override public String toString() {
        String result = "";
        String NEW_LINE = System.getProperty("line.separator");

        result += "{" + NEW_LINE;
        result += "  Speed: " + Speed + NEW_LINE;
        result += "  Acceleration: " + Acceleration + NEW_LINE;
        result += "  Jerk: " + Jerk;
        result += " }";

        return result;
    }

}
