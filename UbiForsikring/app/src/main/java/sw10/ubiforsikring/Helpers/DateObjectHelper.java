package sw10.ubiforsikring.Helpers;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateObjectHelper {
    public static Date CreateDateObject(String dateString) {

        Date date = StringtoDate(dateString, "yyyy-MM-dd'T'HH:mm:ss");
        return date;
    }

    public static Date StringtoDate(String date, String format) {
        SimpleDateFormat simpledateformat = new SimpleDateFormat(format);
        Date stringDate = simpledateformat.parse(date,new ParsePosition(0));
        return stringDate;
    }
}
