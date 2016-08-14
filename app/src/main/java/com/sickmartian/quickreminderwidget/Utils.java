package com.sickmartian.quickreminderwidget;

import org.joda.time.LocalDateTime;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class Utils {
    public static LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public static int getAppSmallIcon() {
        int smallIcon = R.drawable.ic_alarm_on_black_24dp;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            smallIcon = R.drawable.notification_lp;
//        }
        return smallIcon;
    }
}
