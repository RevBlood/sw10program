package sw10.ubiforsikring.Objects.FactObjects;

import android.location.Location;
import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Casper on 11-02-2016.
 */
public class SpatialTemporalInformation {
    public long EntryId;
    public long TripId;
    public Location Point;
    public Location MPoint;
    public Double DistanceToLag;
    public String PathLine;
    public int SecondsToLag;

    public SpatialTemporalInformation(long EntryId, long TripId, Location Point, Location MPoint, double DistanceToLag, String PathLine, int SecondsToLag){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.Point = Point;
        this.MPoint = MPoint;
        this.DistanceToLag = DistanceToLag;
        this.PathLine = PathLine;
        this.SecondsToLag = SecondsToLag;
    }

    public SpatialTemporalInformation(JSONObject objSpatial, JSONObject objTemporal) {
        try {
            this.Point = new Location("");
            this.Point.setLatitude(objSpatial.isNull("pointlat") ? 0 : objSpatial.getDouble("pointlat"));
            this.Point.setLongitude(objSpatial.isNull("pointlng") ? 0 : objSpatial.getDouble("pointlng"));
            this.Point.setTime(deserializeDate(objTemporal.isNull("timestamp") ? "0" : objTemporal.getString("timestamp")).getTime());

            this.MPoint = new Location("");
            this.MPoint.setLatitude(objSpatial.isNull("mpointlat") ? 0 : objSpatial.getDouble("mpointlat"));
            this.MPoint.setLongitude(objSpatial.isNull("mpointlng") ? 0 : objSpatial.getDouble("mpointlng"));
            this.MPoint.setTime(deserializeDate(objTemporal.isNull("timestamp") ? "0" : objTemporal.getString("timestamp")).getTime());

            this.DistanceToLag = objSpatial.isNull("distancetolag") ? 0 : objSpatial.getDouble("distancetolag");
            this.PathLine = objSpatial.isNull("pathline") ? "" : objSpatial.getString("pathline");
            this.SecondsToLag = objTemporal.isNull("secondstolag") ? 0 : objTemporal.getInt("secondstolag");
        }
        catch (Exception e){
            Log.e("Debug", "SpatialInformation - JSONObject:", e);
        }
    }

    public JSONObject serializeSpatialToJSON(){
        JSONObject obj = new JSONObject();
        try{
            if(Point != null) {
                obj.put("pointlat", Point.getLatitude());
                obj.put("pointlng", Point.getLongitude());
            }
            if(MPoint != null) {
                obj.put("mpointlat", MPoint.getLatitude());
                obj.put("mpointlng", MPoint.getLongitude());
            }
            if(DistanceToLag != null) {
                obj.put("distancetolag", DistanceToLag);

            }
            if(PathLine != null) {
                obj.put("pathline", PathLine);
            }
        } catch(Exception e) {
            Log.e("Debug", "SpatialInformation - Serialize:", e);
        }
        return obj;
    }

    public JSONObject serializeTemporalToJSON(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("timestamp", MPoint.getTime());
            obj.put("secondstolag", SecondsToLag);
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
        result.append("  Point: " + "Lat: " + Point.getLatitude() + ", Lng: " + Point.getLongitude() + ", Time: " + Point.getTime() + NEW_LINE);
        result.append("  MPoint: " + "Lat: " + MPoint.getLatitude() + ", Lng: " + MPoint.getLongitude() + ", Time: " + MPoint.getTime() + NEW_LINE);
        result.append("  DistanceToLag: " + DistanceToLag + NEW_LINE);
        result.append("  PathLine: " + PathLine + NEW_LINE);
        result.append("  SecondsToLag: " + SecondsToLag);
        result.append(" }");

        return result.toString();
    }
}
