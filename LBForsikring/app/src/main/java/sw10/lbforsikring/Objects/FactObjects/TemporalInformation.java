package sw10.lbforsikring.Objects.FactObjects;

import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Casper on 11-02-2016.
 */
public class TemporalInformation {
    public long EntryId;
    public long TripId;
    public Date Timestamp;
    public int SecondsToLag;

    public TemporalInformation(long EntryId, long TripId, Date Timestamp, int SecondsToLag){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.Timestamp = Timestamp;
        //this.SecondsToLag = SecondsToLag;
    }

    public TemporalInformation(JSONObject obj){
        try {
            this.Timestamp = deserializeDate(obj.isNull("timestamp") ? "\\/Date(0+0100)\\/" : obj.getString("timestamp"));
            //this.SecondsToLag = obj.isNull("secondstolag") ? 0 : obj.getInt("secondstolag");
        }
        catch (Exception e){
            Log.e("Debug", "TemporalInformation - JSONObject:", e);
        }
    }

    public JSONObject serializeToJSON(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("timestamp", Timestamp.getTime());
            //obj.put("secondstolag", SecondsToLag);
        } catch(Exception e) {
            Log.e("Debug", "TemporalInformation - Serialize:", e);
        }
        return obj;
    }

    private Date deserializeDate(String serializedDate) {
        Date d = new Date();
        String[] out = null;
        if(serializedDate.contains("/")){
            //Isolate the conceiled Long in the DateTime from C#.
            String s = serializedDate;
            out = s.split("\\(");
            out = out[1].split("\\+");
        } else {
            d.setTime(Long.parseLong(serializedDate));
            return d;
        }

        //Use it as time since epoch

        d.setTime(Long.parseLong(out[0]));
        return d;
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append("{" + NEW_LINE);
        result.append("  Timestamp: " + Timestamp);
        result.append(" }");
        //result.append(" SecondsToLag: " + SecondsToLag);

        return result.toString();
    }
}
