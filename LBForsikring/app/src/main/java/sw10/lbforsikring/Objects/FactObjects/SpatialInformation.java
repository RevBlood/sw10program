package sw10.lbforsikring.Objects.FactObjects;

import android.location.Location;

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
}
