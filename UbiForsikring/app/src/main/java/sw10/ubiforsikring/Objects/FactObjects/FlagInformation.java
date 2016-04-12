package sw10.ubiforsikring.Objects.FactObjects;

import android.util.Log;

import org.json.JSONObject;

public class FlagInformation {

    public long EntryId;
    public long TripId;
    public Boolean Speeding;
    public Boolean Accelerating;
    public Boolean Jerking;
    public Boolean Braking;
    public Boolean SteadySpeed;

    public FlagInformation(long EntryId, long TripId, Boolean Speeding, Boolean Accelerating, Boolean Braking, Boolean Jerking, Boolean SteadySpeed){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.Speeding = Speeding;
        this.Accelerating = Accelerating;
        this.Braking = Braking;
        this.Jerking = Jerking;
        this.SteadySpeed = SteadySpeed;
    }

    public FlagInformation(Boolean Speeding, Boolean Accelerating, Boolean Braking, Boolean Jerking){
        this.Speeding = Speeding;
        this.Accelerating = Accelerating;
        this.Braking = Braking;
        this.Jerking = Jerking;
    }

    public FlagInformation(JSONObject jsonObject) {
        try {
            Speeding = !jsonObject.isNull("speeding") && jsonObject.getBoolean("speeding");
            Accelerating = !jsonObject.isNull("accelerating") && jsonObject.getBoolean("accelerating");
            Jerking = !jsonObject.isNull("jerking") && jsonObject.getBoolean("jerking");
            Braking = !jsonObject.isNull("braking") && jsonObject.getBoolean("braking");
            //SteadySpeed = !obj.isNull("steadyspeed") && obj.getBoolean("steadyspeed");
        } catch (Exception e){
            Log.e("Debug", "FlagInformation - JSONObject:", e);
        }
    }

    public JSONObject serializeToJSON(){
        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put("speeding", Speeding);
            jsonObject.put("accelerating", Accelerating);
            jsonObject.put("braking", Braking);
            jsonObject.put("jerking", Jerking);
            //obj.put("steadyspeed", SteadySpeed);
        } catch(Exception e) {
            Log.e("Debug", "FlagInformation - Serialize:", e);
        }

        return jsonObject;
    }

    @Override public String toString() {
        String result = "";
        String NEW_LINE = System.getProperty("line.separator");

        result += "{" + NEW_LINE;
        result += "  Speeding: " + Speeding + NEW_LINE;
        result += "  Accelerating: " + Accelerating + NEW_LINE;
        result += "  Braking: " + Braking + NEW_LINE;
        result += "  Jerking: " + Jerking + NEW_LINE;
        //result.append("  SteadySpeed: " + SteadySpeed);
        result += " }";

        return result;
    }



}
