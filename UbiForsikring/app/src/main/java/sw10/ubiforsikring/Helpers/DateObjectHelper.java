package sw10.ubiforsikring.Helpers;

import java.util.Date;

public class DateObjectHelper {
    public static Date CreateDateObject(String dateString) {
        Date date = new Date();

        if (dateString.contains("/")) {
            //Isolate the Long in the DateTime from C# and use it as time since epoch
            date.setTime(Long.parseLong(dateString.split("\\(")[1].split("\\+")[0]));
        } else {
            date.setTime(Long.parseLong(dateString));
        }

        return date;
    }
}
