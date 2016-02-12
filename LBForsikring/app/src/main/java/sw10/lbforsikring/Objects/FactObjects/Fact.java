package sw10.lbforsikring.Objects.FactObjects;


/**
 * Created by Casper on 11-02-2016.
 */
public class Fact {
    public long EntryId;
    public long TripId;
    public int CarId;
    public FlagInformation Flag;
    public MeasureInformation Measure;
    public SpatialInformation Spatial;
    public TemporalInformation Temporal;

    public Fact(long EntryId, long TripId, int CarId, FlagInformation Flag, MeasureInformation Measure, SpatialInformation Spatial, TemporalInformation Temporal){
        this.EntryId = EntryId;
        this.TripId = TripId;
        this.CarId = CarId;
        this.Flag = Flag;
        this.Measure = Measure;
        this.Spatial = Spatial;
        this.Temporal = Temporal;
    }
}
