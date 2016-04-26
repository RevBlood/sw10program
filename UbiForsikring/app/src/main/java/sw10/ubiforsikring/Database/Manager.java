package sw10.ubiforsikring.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Manager extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "drivelab.db";

    public Manager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // CREATE TABLE SQL STATEMENTS KALDES
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MetaQueries.SQL_CREATE_CAR_INFORMATION);
        db.execSQL(MetaQueries.SQL_CREATE_QUALITY_INFORMATION);
        db.execSQL(MetaQueries.SQL_CREATE_SEGMENT_INFORMATION);
        db.execSQL(MetaQueries.SQL_CREATE_DATE_DIMENSIONS);
        db.execSQL(MetaQueries.SQL_CREATE_TIME_DIMENSIONS);
        db.execSQL(MetaQueries.SQL_CREATE_SUBTRIP_FACT);
        db.execSQL(MetaQueries.SQL_CREATE_GPS_FACT);
        db.execSQL(MetaQueries.SQL_CREATE_TRIP_FACT);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(MetaQueries.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
