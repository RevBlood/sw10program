package sw10.lbforsikring;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Johan Leth Gregersen on 10-02-2016.
 */
public class LBDatabaseHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LBdatabase.db";

    public LBDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // DE ANDRE TABLES SKAL NOK OGSÅ LIGE FIXES ;)
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(dbMetaQueries.SQL_CREATE_GPSFACT);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(dbMetaQueries.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
