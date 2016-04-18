package sw10.ubiforsikring.Helpers;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateObjectHelper {
    public static Date CreateDateObject(String dateString) {


        Date date = StringtoDate(dateString, "yyyy-MM-dd'T'HH:mm:ss");

        /*
        if (dateString.contains("/")) {
            //Isolate the Long in the DateTime from C# and use it as time since epoch
            date.setTime(Long.parseLong(dateString.split("\\(")[1].split("\\+")[0]));
        } else {
            date.setTime(Long.parseLong(dateString));
        }
        */

        return date;
    }

    public static Date StringtoDate(String date, String format) {
        SimpleDateFormat simpledateformat = new SimpleDateFormat(format);
        Date stringDate = simpledateformat.parse(date,new ParsePosition(0));
        return stringDate;
    }
}
