package sw10.lbforsikring.Objects.FactObjects;

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

}
