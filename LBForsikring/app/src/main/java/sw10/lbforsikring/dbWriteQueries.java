package sw10.lbforsikring;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.location.Location;

/**
 * Created by Johan Leth Gregersen on 10-02-2016.
 */
public class dbWriteQueries {

    private LBDatabaseHelper mDBhelper;
    private SQLiteDatabase db;

    public dbWriteQueries(LBDatabaseHelper DBhelper) {
        this.mDBhelper = DBhelper;
        db = mDBhelper.getWritableDatabase();
    }

    public long InsertLocationIntoGPS(Location location) {
        ContentValues tempvalues = new ContentValues();
        tempvalues.put(DataWarehouseContract.GPSFact.COLUMN_NAME_POINT, Double.toString(location.getLongitude()) + "," + Double.toString(location.getLatitude()));
        long NewRowId = db.insert(
                DataWarehouseContract.GPSFact.TABLE_NAME,
                null,
                tempvalues);
        return NewRowId;
    }

    public void InsertIntoGPSfact(ContentValues values){
        long NewRowId;
        NewRowId = db.insert(
                DataWarehouseContract.GPSFact.TABLE_NAME,
                null,
                values);
    }

}
