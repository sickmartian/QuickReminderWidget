package com.sickmartian.quickreminderwidget;

/**
 * Created by ***REMOVED*** on 1/6/17.
 */

public class CustomAlarmTimeValue {

    static final int NONE_INDEX = 0;
    public static int[] customValueCorrespondingValues = new int[]{
            QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME,
            1, 5, 10, 15,
            20, 25, 30, 35,
            40, 45, 60, 90,
            120, 180, 240
    };
    public static int[] customValueCorrespondingNames = new int[]{
            R.string.custom_values_disabled_label,
            R.string.custom_values_1_label,
            R.string.custom_values_5_label,
            R.string.custom_values_10_label,
            R.string.custom_values_15_label,
            R.string.custom_values_20_label,
            R.string.custom_values_25_label,
            R.string.custom_values_30_label,
            R.string.custom_values_35_label,
            R.string.custom_values_40_label,
            R.string.custom_values_45_label,
            R.string.custom_values_60_label,
            R.string.custom_values_90_label,
            R.string.custom_values_120_label,
            R.string.custom_values_180_label,
            R.string.custom_values_240_label
    };
    public static int[] customValueCorrespondingShort = new int[]{
            R.string.custom_values_disabled_label_short,
            R.string.custom_values_1_label_short,
            R.string.custom_values_5_label_short,
            R.string.custom_values_10_label_short,
            R.string.custom_values_15_label_short,
            R.string.custom_values_20_label_short,
            R.string.custom_values_25_label_short,
            R.string.custom_values_30_label_short,
            R.string.custom_values_35_label_short,
            R.string.custom_values_40_label_short,
            R.string.custom_values_45_label_short,
            R.string.custom_values_60_label_short,
            R.string.custom_values_90_label_short,
            R.string.custom_values_120_label_short,
            R.string.custom_values_180_label_short,
            R.string.custom_values_240_label_short
    };

    public static String getCustomValueShortLabel(int minutes) {
        int foundIndex = -1;
        for (int i = 0; i < customValueCorrespondingValues.length; i++) {
            if (customValueCorrespondingValues[i] == minutes) {
                foundIndex = i;
                break;
            }
        }
        if (foundIndex > 0) {
            return App.getAppContext().getString(customValueCorrespondingShort[foundIndex]);
        } else {
            return "";
        }
    }

}
