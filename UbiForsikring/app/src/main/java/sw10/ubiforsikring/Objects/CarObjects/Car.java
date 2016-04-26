package sw10.ubiforsikring.Objects.CarObjects;

import android.util.Log;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import sw10.ubiforsikring.Helpers.DateObjectHelper;

public class Car {
    public int CarId;
    public long IMEI;
    public String Username;



    public Car(JSONObject obj) {
        try {
            this.CarId = obj.getInt("carid");
            this.IMEI = obj.getLong("imei");
            this.Username = obj.isNull("username") ? "" : obj.getString("username");

        } catch (Exception e){
        }
    }
}
