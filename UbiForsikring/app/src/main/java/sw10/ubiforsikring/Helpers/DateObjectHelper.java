package sw10.ubiforsikring.Helpers;

import java.util.Date;

/**
 * Created by Casper on 09-03-2016.
 */
public class DateObjectHelper {

    public static Date CreateDateObject(String dateString) {

            Date date = new Date();
            String[] out = null;
            if(dateString.contains("/")){
                //Isolate the conceiled Long in the DateTime from C#.
                out = dateString.split("\\(");
                out = out[1].split("\\+");
            } else {
                date.setTime(Long.parseLong(dateString));
                return date;
            }

            //Use it as time since epoch
            date.setTime(Long.parseLong(out[0]));
            return date;
    }
}
