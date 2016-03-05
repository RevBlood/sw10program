package sw10.ubiforsikring;

public class TripListEntry {
    public long TripId;
    public long TimeStarted;
    public Long TimeEnded;
    public double Distance = 0;
    public int Optimality;
    public double Cost;

    public boolean IsActive = false;
    public boolean IsProcessing = false;

    public TripListEntry(boolean isActive, boolean isProcessing) {
        IsActive = isActive;
        IsProcessing = isProcessing;
    }

    public TripListEntry(long tripId, long timeStarted, long timeEnded, double distance, int optimality, double cost) {
        TripId = tripId;
        TimeStarted = timeStarted;
        TimeEnded = timeEnded;
        Distance = distance;
        Optimality = optimality;
        Cost = cost;
    }

    public void SetFlags(boolean isActive, boolean isProcessing) {
        IsActive = isActive;
        IsProcessing = isProcessing;
    }
}
