package sw10.lbforsikring;

import android.graphics.Region;

/**
 * Created by Johan Leth Gregersen on 10-02-2016.
 *
 * ALT POSTGRES RELATED ER BLOT GEMT I TEXT FORMAT ATM
 *
 */
public class dbMetaQueries {

    private static final String COMMA_SEP = ",";
    private static final String INTEGER = " INTEGER";
    private static final String REAL  = " REAL";
    private static final String TEXT = " TEXT";
    private static final String BOOLEAN = " BOOLEAN";

    private static final String PRIMARYKEY = " PRIMARY KEY ";
    private static final String FOREIGNKEY = " FOREIGN KEY ";
    private static final String REFERENCES = " REFERENCES ";


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

    public static final String SQL_CREATE_GPS_FACT =
            "CREATE TABLE " + DataWarehouseContract.GPSFact.TABLE_NAME + " (" +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_ENTRY_ID + INTEGER + PRIMARYKEY + "AUTOINCREMENT" + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_CAR_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_TRIP_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_QUALITY_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SEGMENT_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_DATE_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_TIME_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_POINT + TEXT + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_MPOINT + TEXT + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_PATHLINE + TEXT + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SPEED + REAL + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_MAX_SPEED + INTEGER + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_ACCELERATION + REAL + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_JERK + REAL + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SPEEDING + BOOLEAN + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_BRAKING + BOOLEAN + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_STEADYSPEED + BOOLEAN + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_ACCELERATING + BOOLEAN +  COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_JERKING + BOOLEAN + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_DISTANCE_TO_LAG + REAL + COMMA_SEP +
                    DataWarehouseContract.GPSFact.COLUMN_NAME_SECONDS_TO_LAG + INTEGER + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.GPSFact.COLUMN_NAME_CAR_ID + ")" +
                    REFERENCES + DataWarehouseContract.CarInformation.TABLE_NAME + "(" +
                                 DataWarehouseContract.CarInformation.COLUMN_NAME_CAR_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.GPSFact.COLUMN_NAME_TRIP_ID + ")" +
                    REFERENCES + DataWarehouseContract.TripFact.TABLE_NAME + "(" +
                                 DataWarehouseContract.TripFact.COLUMN_NAME_TRIP_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.GPSFact.COLUMN_NAME_QUALITY_ID + ")" +
                    REFERENCES + DataWarehouseContract.QualityInformation.TABLE_NAME + "(" +
                                 DataWarehouseContract.QualityInformation.COLUMN_NAME_QUALITY_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.GPSFact.COLUMN_NAME_SEGMENT_ID + ")" +
                    REFERENCES + DataWarehouseContract.SegmentInformation.TABLE_NAME + "(" +
                                 DataWarehouseContract.SegmentInformation.COLUMN_NAME_SEGMENT_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.GPSFact.COLUMN_NAME_DATE_ID + ")" +
                    REFERENCES + DataWarehouseContract.DateDimension.TABLE_NAME + "(" +
                                 DataWarehouseContract.DateDimension.COLUMN_NAME_DATE_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.GPSFact.COLUMN_NAME_TIME_ID + ")" +
                    REFERENCES + DataWarehouseContract.TimeDimension.TABLE_NAME + "(" +
                                 DataWarehouseContract.TimeDimension.COLUMN_NAME_TIME_ID + "))";

    public static final String SQL_CREATE_TRIP_FACT =
            "CREATE TABLE " + DataWarehouseContract.TripFact.TABLE_NAME + " (" +
                    DataWarehouseContract.TripFact.COLUMN_NAME_TRIP_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_PREVIOUS_TRIP_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_CAR_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_START_DATE_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_START_TIME_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_END_DATE_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_END_TIME_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_SECONDS_DRIVEN + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_METERS_DRIVEN + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_PRICE + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_OPTIMAL_SCORE + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_TRIP_SCORE + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_METERS_SPED + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_TIME_SPED + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_STEADY_SPEED_DISTANCE + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_STEADY_SPEED_TIME + REAL + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_ACCELERATION_COUNT + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_JERK_COUNT + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_BRAKE_COUNT + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_SECONDS_TO_LAG + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_ROADTYPE_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_CRITICAL_TIME_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_SPEED_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_ACCELERATION_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_JERK_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_BRAKE_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TripFact.COLUMN_NAME_DATA_QUALITY + REAL + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.TripFact.COLUMN_NAME_PREVIOUS_TRIP_ID + ")" +
                    REFERENCES + DataWarehouseContract.TripFact.TABLE_NAME + "(" +
                                 DataWarehouseContract.TripFact.COLUMN_NAME_TRIP_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.TripFact.COLUMN_NAME_CAR_ID + ")" +
                    REFERENCES + DataWarehouseContract.CarInformation.TABLE_NAME + "(" +
                                 DataWarehouseContract.CarInformation.COLUMN_NAME_CAR_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.TripFact.COLUMN_NAME_START_TIME_ID + ")" +
                    REFERENCES + DataWarehouseContract.TimeDimension.TABLE_NAME + "(" +
                                 DataWarehouseContract.TimeDimension.COLUMN_NAME_TIME_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.TripFact.COLUMN_NAME_END_TIME_ID + ")" +
                    REFERENCES + DataWarehouseContract.TimeDimension.TABLE_NAME + "(" +
                                 DataWarehouseContract.TimeDimension.COLUMN_NAME_TIME_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.TripFact.COLUMN_NAME_START_DATE_ID + ")" +
                    REFERENCES + DataWarehouseContract.DateDimension.TABLE_NAME + "(" +
                                 DataWarehouseContract.DateDimension.COLUMN_NAME_DATE_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.TripFact.COLUMN_NAME_END_DATE_ID + ")" +
                    REFERENCES + DataWarehouseContract.DateDimension.TABLE_NAME + "(" +
                                 DataWarehouseContract.DateDimension.COLUMN_NAME_DATE_ID + "))";

    public static final String SQL_CREATE_SUBTRIP_FACT =
            "CREATE TABLE " + DataWarehouseContract.SubTripFact.TABLE_NAME + " (" +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_SUBTRIP_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_PREVIOUS_SUBTRIP_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_CAR_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_SEGMENT_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_COMPETETION_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_SECONDS_DRIVEN + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_METERS_DRIVEN + REAL + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_OPTIMAL_SUBTRIP_SCORE + REAL + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_SUBTRIP_SCORE + REAL + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_ROADTYPE + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_METERS_SPED + REAL + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_TIME_SPED + REAL + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_STEADY_SPEED_DISTANCE + REAL + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_STEADY_SPEED_TIME + REAL + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_ACCELERATION_COUNT + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_JERK_COUNT + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_BRAKE_COUNT + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_CRITICAL_TIME_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_SPEED_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_ACCELERATION_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_JERK_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_BRAKE_INTERVALS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SubTripFact.COLUMN_NAME_DATA_QUALITY + REAL + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.SubTripFact.COLUMN_NAME_PREVIOUS_SUBTRIP_ID + ")" +
                    REFERENCES + DataWarehouseContract.SubTripFact.TABLE_NAME + "(" +
                                 DataWarehouseContract.SubTripFact.COLUMN_NAME_SUBTRIP_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.SubTripFact.COLUMN_NAME_CAR_ID + ")" +
                    REFERENCES + DataWarehouseContract.CarInformation.TABLE_NAME + "(" +
                                 DataWarehouseContract.CarInformation.COLUMN_NAME_CAR_ID + ")" + COMMA_SEP +
                    FOREIGNKEY + "(" + DataWarehouseContract.SubTripFact.COLUMN_NAME_SEGMENT_ID + ")" +
                    REFERENCES + DataWarehouseContract.SegmentInformation.TABLE_NAME + "(" +
                                 DataWarehouseContract.SegmentInformation.COLUMN_NAME_SEGMENT_ID + "))";

    public static final String SQL_CREATE_TIME_DIMENSIONS =
            "CREATE TABLE " + DataWarehouseContract.TimeDimension.TABLE_NAME + " (" +
                    DataWarehouseContract.TimeDimension.COLUMN_NAME_TIME_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TimeDimension.COLUMN_NAME_HOUR + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TimeDimension.COLUMN_NAME_MINUTE + INTEGER + COMMA_SEP +
                    DataWarehouseContract.TimeDimension.COLUMN_NAME_SECOND + INTEGER + ")";

    public static final String SQL_CREATE_DATE_DIMENSIONS =
            "CREATE TABLE " + DataWarehouseContract.DateDimension.TABLE_NAME + " (" +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_DATE_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_YEAR + INTEGER + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_MONTH + INTEGER + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_DAY + INTEGER + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_DAY_OF_WEEK + INTEGER + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_WEEKEND + BOOLEAN + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_HOLIDAY + BOOLEAN + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_QUARTER + INTEGER + COMMA_SEP +
                    DataWarehouseContract.DateDimension.COLUMN_NAME_SEASON + INTEGER + ")";

    public static final String SQL_CREATE_CAR_INFORMATION =
            "CREATE TABLE " + DataWarehouseContract.CarInformation.TABLE_NAME + " (" +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_CAR_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_CAR_TYPE + TEXT + COMMA_SEP +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_BRAND + TEXT + COMMA_SEP +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_MODEL + TEXT + COMMA_SEP +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_FUEL_CONSUMPTION + REAL + COMMA_SEP +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_ENERGY_CONSUMPTION + REAL + COMMA_SEP +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_WEIGHT + REAL + COMMA_SEP +
                    DataWarehouseContract.CarInformation.COLUMN_NAME_CAPACITY + INTEGER + ")";

    public static final String SQL_CREATE_QUALITY_INFORMATION =
            "CREATE TABLE " + DataWarehouseContract.QualityInformation.TABLE_NAME + " (" +
                    DataWarehouseContract.QualityInformation.COLUMN_NAME_QUALITY_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.QualityInformation.COLUMN_NAME_SATELLITES + INTEGER + COMMA_SEP +
                    DataWarehouseContract.QualityInformation.COLUMN_NAME_HDOP + REAL + ")";

    public static final String SQL_CREATE_SEGMENT_INFORMATION =
            "CREATE TABLE " + DataWarehouseContract.SegmentInformation.TABLE_NAME + " (" +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_SEGMENT_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_OSM_ID + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_ROADNAME + TEXT + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_ROADTYPE + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_ONEWAY + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_BRIDGE + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_TUNNEL + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_SPEED_BACKWARDS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_SPEED_FORWARDS + INTEGER + COMMA_SEP +
                    DataWarehouseContract.SegmentInformation.COLUMN_NAME_SEGMENT_LINE + TEXT + ")";
}
