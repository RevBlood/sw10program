package sw10.lbforsikring.Objects.FactObjects;

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
        this.SecondsToLag = SecondsToLag;
    }
}
