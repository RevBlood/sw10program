package sw10.ubiforsikring;

import android.content.Context;

public class TimeStringGenerator {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long WEEK = 7 * DAY;

    public static String Generate(long time, Context context) {
        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return null;
        }

        final long elapsed = now - time;

        if (elapsed < 2* MINUTE) {
            return String.format(context.getString(R.string.TripDescriptionText), context.getString(R.string.TimeNow));

        } else if (elapsed < HOUR) {
            return String.format(context.getString(R.string.TripDescriptionText),
                    String.format(context.getString(R.string.TimeMinutes), (int) Math.floor(elapsed / MINUTE)));

        } else if (elapsed < 2 * HOUR) {
            return String.format(context.getString(R.string.TripDescriptionText), context.getString(R.string.TimeHour));

        } else if (elapsed < DAY) {
            return String.format(context.getString(R.string.TripDescriptionText),
                    String.format(context.getString(R.string.TimeHours), (int) Math.floor(elapsed / HOUR)));

        } else if (elapsed < 2 * DAY) {
            return String.format(context.getString(R.string.TripDescriptionText), context.getString(R.string.TimeDay));

        } else if (elapsed < 3 * DAY) {
            return String.format(context.getString(R.string.TripDescriptionText), context.getString(R.string.TimeTwoDays));

        } else if (elapsed < WEEK) {
            return String.format(context.getString(R.string.TripDescriptionText),
                    String.format(context.getString(R.string.TimeDays), (int) Math.floor(elapsed / DAY)));

        } else if (elapsed < 2 * WEEK) {
            return String.format(context.getString(R.string.TripDescriptionText), context.getString(R.string.TimeWeek));

        } else if (elapsed < 4 * WEEK) {
            return String.format(context.getString(R.string.TripDescriptionText),
                    String.format(context.getString(R.string.TimeWeeks), (int) Math.floor(elapsed / WEEK)));

        } else if (elapsed < 8 * WEEK) {
            return String.format(context.getString(R.string.TripDescriptionText), context.getString(R.string.TimeMonth));

        } else if (elapsed < 52 * WEEK) {
            return String.format(context.getString(R.string.TripDescriptionText),
                    String.format(context.getString(R.string.TimeMonths), (int) Math.floor(elapsed / (WEEK * 4))));

        } else {
            return String.format(context.getString(R.string.TripDescriptionText), context.getString(R.string.TimeYear));
        }
    }
}
