package sw10.lbforsikring;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

/**
 * Created by Johan Leth Gregersen on 10-02-2016.
 */
public class dbWriteQueries {

    private LBDatabaseHelper mDBhelper;

    public dbWriteQueries(LBDatabaseHelper mDBhelper) {
        this.mDBhelper = mDBhelper;
    }

    SQLiteDatabase db = mDBhelper.getWritableDatabase();

    public void InsertIntoGPSfact(ContentValues values){
        long NewRowId;
        NewRowId = db.insert(
                DataWarehouseContract.GPSFact.TABLE_NAME,
                null,
                values);

    }

}
