package sw10.lbforsikring.Objects.FactObjects;

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



}
