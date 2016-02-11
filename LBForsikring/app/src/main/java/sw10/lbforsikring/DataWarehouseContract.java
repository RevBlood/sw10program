package sw10.lbforsikring;

import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.database.sqlite.*;

/**
 * Created by Johan Leth Gregersen on 10-02-2016.
 */
public final class DataWarehouseContract {

    public DataWarehouseContract(){}

    /* FACT TABLES */

    public static abstract class GPSFact implements BaseColumns {

        public static final String TABLE_NAME = "gpsfact";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_CAR_ID = "carid";
        public static final String COLUMN_NAME_TRIP_ID = "tripid";
        public static final String COLUMN_NAME_QUALITY_ID = "qualityid";
        public static final String COLUMN_NAME_SEGMENT_ID = "segmentid";
        public static final String COLUMN_NAME_DATE_ID = "dateid";
        public static final String COLUMN_NAME_TIME_ID = "timeid";
        public static final String COLUMN_NAME_POINT = "point";
        public static final String COLUMN_NAME_MPOINT = "mpoint";
        public static final String COLUMN_NAME_PATHLINE = "pathline";
        public static final String COLUMN_NAME_SPEED = "speed";
        public static final String COLUMN_NAME_MAX_SPEED = "maxspeed";
        public static final String COLUMN_NAME_ACCELERATION = "acceleration";
        public static final String COLUMN_NAME_JERK = "jerk";
        public static final String COLUMN_NAME_SPEEDING = "speeding";
        public static final String COLUMN_NAME_BRAKING = "braking";
        public static final String COLUMN_NAME_STEADYSPEED = "steadyspeed";
        public static final String COLUMN_NAME_ACCELERATING = "accelerating";
        public static final String COLUMN_NAME_JERKING = "jerking";
        public static final String COLUMN_NAME_DISTANCE_TO_LAG = "distancetolag";
        public static final String COLUMN_NAME_SECONDS_TO_LAG = "secondstolag";

    }

    public static abstract class TripFact implements BaseColumns {

        public static final String TABLE_NAME = "tripfact";
        public static final String COLUMN_NAME_TRIP_ID = "tripid";
        public static final String COLUMN_NAME_PREVIOUS_TRIP_ID = "previoustripid";
        public static final String COLUMN_NAME_CAR_ID = "carid";
        public static final String COLUMN_NAME_START_DATE_ID = "startdateid";
        public static final String COLUMN_NAME_START_TIME_ID = "starttimeid";
        public static final String COLUMN_NAME_END_DATE_ID = "enddateid";
        public static final String COLUMN_NAME_END_TIME_ID = "endtimeid";
        public static final String COLUMN_NAME_SECONDS_DRIVEN = "secondsdriven";
        public static final String COLUMN_NAME_METERS_DRIVEN = "metersdriven";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_OPTIMAL_SCORE = "optimalscore";
        public static final String COLUMN_NAME_TRIP_SCORE = "tripscore";
        public static final String COLUMN_NAME_METERS_SPED = "meterssped";
        public static final String COLUMN_NAME_TIME_SPED = "timesped";
        public static final String COLUMN_NAME_STEADY_SPEED_DISTANCE = "steadyspeeddistance";
        public static final String COLUMN_NAME_STEADY_SPEED_TIME = "steadyspeedtime";
        public static final String COLUMN_NAME_ACCELERATION_COUNT = "accelerationcount";
        public static final String COLUMN_NAME_JERK_COUNT = "jerkcount";
        public static final String COLUMN_NAME_BRAKE_COUNT = "brakecount";
        public static final String COLUMN_NAME_SECONDS_TO_LAG = "secondstolag";
        public static final String COLUMN_NAME_ROADTYPE_INTERVALS = "roadtypesinterval";
        public static final String COLUMN_NAME_CRITICAL_TIME_INTERVALS = "criticaltimeinterval";
        public static final String COLUMN_NAME_SPEED_INTERVALS = "speedinterval";
        public static final String COLUMN_NAME_ACCELERATION_INTERVALS = "accelerationinterval";
        public static final String COLUMN_NAME_JERK_INTERVALS = "jerkinterval";
        public static final String COLUMN_NAME_BRAKE_INTERVALS = "brakeinterval";
        public static final String COLUMN_NAME_DATA_QUALITY = "dataquality";

    }

    public static abstract class SubTripFact implements BaseColumns {

        public static final String TABLE_NAME = "subtripfact";
        public static final String COLUMN_NAME_SUBTRIP_ID = "subtripid";
        public static final String COLUMN_NAME_PREVIOUS_SUBTRIP_ID = "previoussubtripid";
        public static final String COLUMN_NAME_CAR_ID = "carid";
        public static final String COLUMN_NAME_SEGMENT_ID = "segmentid";
        public static final String COLUMN_NAME_COMPETETION_ID = "competetionid";
        public static final String COLUMN_NAME_SECONDS_DRIVEN = "secondsdriven";
        public static final String COLUMN_NAME_METERS_DRIVEN = "metersdriven";
        public static final String COLUMN_NAME_OPTIMAL_SUBTRIP_SCORE = "optimalsubtripscore";
        public static final String COLUMN_NAME_SUBTRIP_SCORE = "subtripscore";
        public static final String COLUMN_NAME_ROADTYPE = "roadtype";
        public static final String COLUMN_NAME_METERS_SPED = "meterssped";
        public static final String COLUMN_NAME_TIME_SPED = "timesped";
        public static final String COLUMN_NAME_STEADY_SPEED_DISTANCE = "steadyspeeddistance";
        public static final String COLUMN_NAME_STEADY_SPEED_TIME = "steadyspeedtime";
        public static final String COLUMN_NAME_ACCELERATION_COUNT = "accelerationcount";
        public static final String COLUMN_NAME_JERK_COUNT = "jerkcount";
        public static final String COLUMN_NAME_BRAKE_COUNT = "brakecount";;
        public static final String COLUMN_NAME_CRITICAL_TIME_INTERVALS = "criticaltimeinterval";
        public static final String COLUMN_NAME_SPEED_INTERVALS = "speedinterval";
        public static final String COLUMN_NAME_ACCELERATION_INTERVALS = "accelerationinterval";
        public static final String COLUMN_NAME_JERK_INTERVALS = "jerkinterval";
        public static final String COLUMN_NAME_BRAKE_INTERVALS = "brakeinterval";
        public static final String COLUMN_NAME_DATA_QUALITY = "dataquality";

    }

    /* INFORMATION TABLES */

    public static abstract class CarInformation implements BaseColumns {

        public static final String TABLE_NAME = "carinformation";
        public static final String COLUMN_NAME_CAR_ID = "carid";
        public static final String COLUMN_NAME_CAR_TYPE = "cartype";
        public static final String COLUMN_NAME_BRAND = "brand";
        public static final String COLUMN_NAME_MODEL = "model";
        public static final String COLUMN_NAME_FUEL_CONSUMPTION = "fuelconsumption";
        public static final String COLUMN_NAME_ENERGY_CONSUMPTION = "energyconsumption";
        public static final String COLUMN_NAME_WEIGHT = "weight";
        public static final String COLUMN_NAME_CAPACITY = "capacity";

    }

    public static abstract class QualityInformation implements BaseColumns {

        public static final String TABLE_NAME = "qualityinformation";
        public static final String COLUMN_NAME_QUALITY_ID = "qualityid";
        public static final String COLUMN_NAME_SATELLITES = "satellites";
        public static final String COLUMN_NAME_HDOP = "hdop";

    }

    public static abstract class SegmentInformation implements BaseColumns {

        public static final String TABLE_NAME = "segmentinformation";
        public static final String COLUMN_NAME_SEGMENT_ID = "segmentid";
        public static final String COLUMN_NAME_OSM_ID = "osmid";
        public static final String COLUMN_NAME_ROADNAME = "roadname";
        public static final String COLUMN_NAME_ROADTYPE = "roadtype";
        public static final String COLUMN_NAME_ONEWAY = "oneway";
        public static final String COLUMN_NAME_BRIDGE = "bridge";
        public static final String COLUMN_NAME_TUNNEL = "tunnel";
        public static final String COLUMN_NAME_SPEED_BACKWARDS = "speedbackward";
        public static final String COLUMN_NAME_SPEED_FORWARDS = "speedforward";
        public static final String COLUMN_NAME_SEGMENT_LINE = "segmentline";
    }

    /* DIMENSIONS */

    public static abstract class TimeDimension implements BaseColumns {

        public static final String TABLE_NAME = "dimtime";
        public static final String COLUMN_NAME_TIME_ID = "timeid";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_MINUTE = "minute";
        public static final String COLUMN_NAME_SECOND = "second";

    }

    public static abstract class DateDimension implements BaseColumns {

        public static final String TABLE_NAME = "dimdate";
        public static final String COLUMN_NAME_DATE_ID = "dateid";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_DAY_OF_WEEK = "dayofweek";
        public static final String COLUMN_NAME_WEEKEND = "weekend";
        public static final String COLUMN_NAME_HOLIDAY = "holiday";
        public static final String COLUMN_NAME_QUARTER = "quarter";
        public static final String COLUMN_NAME_SEASON = "season";

    }

}
