package sw10.ubiforsikring.Objects.FactObjects;

import android.location.Location;

import org.json.JSONObject;

import java.util.Date;

import sw10.ubiforsikring.Helpers.DateObjectHelper;

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

    public SpatialTemporalInformation(Location point) {
        Point = point;
    }

    public SpatialTemporalInformation(JSONObject objSpatial, JSONObject objTemporal) {
        try {
            this.Point = new Location("");
            this.Point.setLatitude(objSpatial.isNull("pointlat") ? 0 : objSpatial.getDouble("pointlat"));
            this.Point.setLongitude(objSpatial.isNull("pointlng") ? 0 : objSpatial.getDouble("pointlng"));
            this.Point.setTime(DateObjectHelper.CreateDateObject(objTemporal.isNull("timestamp") ? "0" : objTemporal.getString("timestamp")).getTime());

            this.MPoint = new Location("");
            this.MPoint.setLatitude(objSpatial.isNull("mpointlat") ? 0 : objSpatial.getDouble("mpointlat"));
            this.MPoint.setLongitude(objSpatial.isNull("mpointlng") ? 0 : objSpatial.getDouble("mpointlng"));
            this.MPoint.setTime(DateObjectHelper.CreateDateObject(objTemporal.isNull("timestamp") ? "0" : objTemporal.getString("timestamp")).getTime());

            this.DistanceToLag = objSpatial.isNull("distancetolag") ? 0 : objSpatial.getDouble("distancetolag");
            this.PathLine = objSpatial.isNull("pathline") ? "" : objSpatial.getString("pathline");
            this.SecondsToLag = objTemporal.isNull("secondstolag") ? 0 : objTemporal.getInt("secondstolag");
        } catch (Exception e){
        }
    }

    public JSONObject serializeSpatialToJSON(){
        JSONObject jsonObject = new JSONObject();
        try{
            if(Point != null) {
                jsonObject.put("pointlat", Point.getLatitude());
                jsonObject.put("pointlng", Point.getLongitude());
            }
            /*
            if(MPoint != null) {
                jsonObject.put("mpointlat", MPoint.getLatitude());
                jsonObject.put("mpointlng", MPoint.getLongitude());
            }

            if(DistanceToLag != null) {
                jsonObject.put("distancetolag", DistanceToLag);

            }
            */
            if(PathLine != null) {
                jsonObject.put("pathline", PathLine);
            }

        } catch(Exception e) {
        }

        return jsonObject;
    }

    public JSONObject serializeTemporalToJSON(){
        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put("timestamp", Point.getTime());
            //jsonObject.put("secondstolag", SecondsToLag);
        } catch(Exception e) {
        }

        return jsonObject;
    }

    private Date deserializeDate(String serializedDate) {
        Date date = new Date();
        if(serializedDate.contains("/")){
            //Isolate the Long in the DateTime from C# and use it as time since epoch
            date.setTime(Long.parseLong(serializedDate.split("\\(")[1].split("\\+")[0]));
        } else {
            date.setTime(Long.parseLong(serializedDate));
        }

        return date;
    }

    @Override public String toString() {
        String result = "";
        String NEW_LINE = System.getProperty("line.separator");

        result += "{" + NEW_LINE;
        result += "  Point: " + "Lat: " + Point.getLatitude() + ", Lng: " + Point.getLongitude() + ", Time: " + Point.getTime() + NEW_LINE;
        result += "  MPoint: " + "Lat: " + MPoint.getLatitude() + ", Lng: " + MPoint.getLongitude() + ", Time: " + MPoint.getTime() + NEW_LINE;
        result += "  DistanceToLag: " + DistanceToLag + NEW_LINE;
        result += "  PathLine: " + PathLine + NEW_LINE;
        result += "  SecondsToLag: " + SecondsToLag;
        result += " }";

        return result;
    }
}
