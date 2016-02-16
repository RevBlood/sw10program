package sw10.lbforsikring.Objects.FactObjects;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Casper on 11-02-2016.
 */
public class FlagInformation {

    public long EntryId;
    public long TripId;
    public Boolean Speeding;
    public Boolean Accelerating;
    public Boolean Jerking;
    public Boolean Braking;
    public Boolean SteadySpeed;

    public FlagInformation(long EntryId, long TripId, Boolean Speeding, Boolean Accelerating, Boolean Jerking, Boolean Braking, Boolean SteadySpeed){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.Speeding = Speeding;
        this.Accelerating = Accelerating;
        this.Jerking = Jerking;
        this.Braking = Braking;
        this.SteadySpeed = SteadySpeed;
    }

    public FlagInformation(JSONObject obj) {
        try {
            this.Speeding = obj.isNull("speeding") ? false : obj.getBoolean("speeding");
            this.Accelerating = obj.isNull("accelerating") ? false : obj.getBoolean("accelerating");
            this.Jerking = obj.isNull("jerking") ? false : obj.getBoolean("jerking");
            this.Braking = obj.isNull("braking") ? false : obj.getBoolean("braking");
            this.SteadySpeed = obj.isNull("steadyspeed") ? false : obj.getBoolean("steadyspeed");
        }
        catch (Exception e){
            Log.e("Debug", "FlagInformation - JSONObject:", e);
        }
    }

    public JSONObject serializeToJSON(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("speeding", Speeding);
            obj.put("accelerating", Accelerating);
            obj.put("jerking", Jerking);
            obj.put("braking", Braking);
            obj.put("steadyspeed", SteadySpeed);

        } catch(Exception e) {
            Log.e("Debug", "FlagInformation - Serialize:", e);
        }
        return obj;
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append("{" + NEW_LINE);
        result.append("  Speeding: " + Speeding + NEW_LINE);
        result.append("  Accelerating: " + Accelerating + NEW_LINE);
        result.append("  Jerking: " + Jerking + NEW_LINE );
        result.append("  Braking: " + Braking + NEW_LINE);
        result.append("  SteadySpeed: " + SteadySpeed);
        result.append(" }");

        return result.toString();
    }



}
