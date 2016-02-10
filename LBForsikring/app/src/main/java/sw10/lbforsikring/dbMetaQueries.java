package sw10.lbforsikring;

/**
 * Created by Johan Leth Gregersen on 10-02-2016.
 */
public class dbMetaQueries {

    private static final String COMMA_SEP = ",";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " +
                    DataWarehouseContract.TripFact.TABLE_NAME + COMMA_SEP +
                    DataWarehouseContract.GPSFact.TABLE_NAME + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.TABLE_NAME + COMMA_SEP +
                    DataWarehouseContract.DateDimension.TABLE_NAME + COMMA_SEP +
                    DataWarehouseContract.TimeDimension.TABLE_NAME + COMMA_SEP +
                    DataWarehouseContract.QualityInformation.TABLE_NAME + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.TABLE_NAME + COMMA_SEP +
                    DataWarehouseContract.CarInformation.TABLE_NAME;

    public static final String SQL_CREATE_GPSFACT =
            "CREATE TABLE " + DataWarehouseContract.GPSFact.TABLE_NAME + " (" +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_ENTRY_ID + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_CAR_ID + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_TRIP_ID + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_QUALITY_ID + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SEGMENT_ID + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_DATE_ID + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_TIME_ID + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_POINT + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_MPOINT + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_PATHLINE + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SPEED + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_MAX_SPEED + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_ACCELERATION + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_JERK + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SPEEDING + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_BRAKING + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_STEADYSPEED + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_DISTANCE_TO_LAG + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SECONDS_TO_LAG + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_ACCELERATING + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_JERKING + ")";

}
