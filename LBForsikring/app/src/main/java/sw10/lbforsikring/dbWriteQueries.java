package sw10.lbforsikring;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

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
        rowsToInsert.put(DataWarehouseContract.GPSFact.COLUMN_NAME_DATE_ID, ToDBDate(location.getTime()));
        rowsToInsert.put(DataWarehouseContract.GPSFact.COLUMN_NAME_TIME_ID, ToDBTime(location.getTime()));

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

    private int ToDBDate(long unixTime) {
        Date timestamp = new java.util.Date(unixTime);
        String dbDate = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        dbDate += calendar.get(Calendar.YEAR);
        dbDate += String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        dbDate += String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        Log.d("Debug", dbDate);
        return Integer.parseInt(dbDate);
    }

    private int ToDBTime(long unixTime) {
        Date timestamp = new java.util.Date(unixTime);
        String dbTime = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        dbTime += String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        dbTime += String.format("%02d", calendar.get(Calendar.MINUTE));
        dbTime += String.format("%02d", calendar.get(Calendar.SECOND));
        Log.d("Debug", dbTime);
        return Integer.parseInt(dbTime);
    }
}
