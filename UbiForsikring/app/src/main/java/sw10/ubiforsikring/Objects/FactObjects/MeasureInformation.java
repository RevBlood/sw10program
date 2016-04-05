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

    public MeasureInformation(JSONObject obj) {
        try {
            this.Speed = obj.isNull("speed") ? 0 : obj.getDouble("speed");
            this.Acceleration = obj.isNull("acceleration") ? 0 : obj.getDouble("acceleration");
            this.Jerk = obj.isNull("jerk") ? 0 : obj.getDouble("jerk");
        }
        catch (Exception e){
            Log.e("Debug", "MeasureInformation - JSONObject:", e);
        }
    }

    public JSONObject serializeToJSON(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("speed", Speed);
            obj.put("acceleration", Acceleration);
            obj.put("jerk", Jerk);
        } catch(Exception e) {
            Log.e("Debug", "MeasureInformation - Serialize:", e);
        }
        return obj;
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append("{" + NEW_LINE);
        result.append("  Speed: " + Speed + NEW_LINE);
        result.append("  Acceleration: " + Acceleration + NEW_LINE);
        result.append("  Jerk: " + Jerk);
        result.append(" }");

        return result.toString();
    }

}
