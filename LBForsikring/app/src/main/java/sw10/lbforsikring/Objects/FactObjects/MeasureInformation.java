package sw10.lbforsikring.Objects.FactObjects;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Casper on 11-02-2016.
 */
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
            obj.put("accelerating", Acceleration);
            obj.put("jerk", Jerk);
        } catch(Exception e) {
            Log.e("Debug", "MeasureInformation - Serialize:", e);
        }
        return obj;
    }
}
