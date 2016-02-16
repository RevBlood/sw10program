package sw10.lbforsikring.Objects.FactObjects;

import android.location.Location;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Casper on 11-02-2016.
 */
public class SpatialInformation {
    public long EntryId;
    public long TripId;
    public Location Point;
    public Location MPoint;
    public double DistanceToLag;
    public String PathLine;

    public SpatialInformation(long EntryId, long TripId, Location Point, Location MPoint, double DistanceToLag, String PathLine){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.Point = Point;
        this.MPoint = MPoint;
        this.DistanceToLag = DistanceToLag;
        this.PathLine = PathLine;
    }

    public SpatialInformation(JSONObject obj) {
        try {
            this.Point = new Location("");
            this.Point.setLatitude(obj.isNull("pointlat") ? 0 : obj.getDouble("pointlat"));
            this.Point.setLongitude(obj.isNull("pointlng") ? 0 : obj.getDouble("pointlng"));

            this.MPoint = new Location("");
            this.MPoint.setLatitude(obj.isNull("mpointlat") ? 0 : obj.getDouble("mpointlat"));
            this.MPoint.setLongitude(obj.isNull("mpointlng") ? 0 : obj.getDouble("mpointlng"));

            this.DistanceToLag = obj.isNull("distancetolag") ? 0 : obj.getDouble("distancetolag");
            this.PathLine = obj.isNull("pathline") ? "" : obj.getString("pathline");
        }
        catch (Exception e){
            Log.e("Debug", "SpatialInformation - JSONObject:", e);
        }
    }

    public JSONObject serializeToJSON(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("pointlat", Point.getLatitude());
            obj.put("pointlng", Point.getLongitude());
            obj.put("mpointlat", MPoint.getLatitude());
            obj.put("mpointlng", MPoint.getLongitude());
            obj.put("distancetolag", DistanceToLag);
            obj.put("pathline", PathLine);
        } catch(Exception e) {
            Log.e("Debug", "SpatialInformation - Serialize:", e);
        }
        return obj;
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append("{" + NEW_LINE);
        result.append("  Point: " + "Lat: " + Point.getLatitude() + ", Lng: " + Point.getLongitude() + ", Time: " + Point.getTime() + NEW_LINE);
        result.append("  MPoint: " + "Lat: " + MPoint.getLatitude() + ", Lng: " + MPoint.getLongitude() + ", Time: " + MPoint.getTime() + NEW_LINE);
        result.append("  DistanceToLag: " + DistanceToLag + NEW_LINE);
        result.append("  PathLine: " + PathLine);
        result.append(" }");

        return result.toString();
    }
}
