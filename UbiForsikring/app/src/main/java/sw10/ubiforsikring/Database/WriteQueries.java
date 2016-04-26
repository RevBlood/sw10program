package sw10.ubiforsikring.Database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import sw10.ubiforsikring.Helpers.MeasureHelper;

public class WriteQueries {

    private Manager mDBManager;
    private SQLiteDatabase mDB;

    public WriteQueries(Manager DBhelper) {
        mDBManager = DBhelper;
        mDB = mDBManager.getWritableDatabase();
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
