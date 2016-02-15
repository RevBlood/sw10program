package sw10.lbforsikring;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import sw10.lbforsikring.Helpers.MeasureHelper;

/**
 * Created by Johan Leth Gregersen on 10-02-2016.
 */
public class dbWriteQueries {

    private LBDatabaseHelper mDBhelper;
    private SQLiteDatabase mDB;

    public dbWriteQueries(LBDatabaseHelper DBhelper) {
        mDBhelper = DBhelper;
        mDB = mDBhelper.getWritableDatabase();
    }

    public long InsertLocationIntoGPS(Location location) {
        ContentValues rowsToInsert = new ContentValues();
        rowsToInsert.put(DataWarehouseContract.GPSFact.COLUMN_NAME_POINT, Double.toString(location.getLongitude()) + "," + Double.toString(location.getLatitude()));
        rowsToInsert.put(DataWarehouseContract.GPSFact.COLUMN_NAME_DATE_ID, MeasureHelper.DBDate(location.getTime()));
        rowsToInsert.put(DataWarehouseContract.GPSFact.COLUMN_NAME_TIME_ID, MeasureHelper.DBTime(location.getTime()));

        return mDB.insert(
                DataWarehouseContract.GPSFact.TABLE_NAME,
                null,
                rowsToInsert);
    }

    public long InsertIntoGPSfact(ContentValues values){
        return mDB.insert(
                DataWarehouseContract.GPSFact.TABLE_NAME,
                null,
                values);
    }
}
