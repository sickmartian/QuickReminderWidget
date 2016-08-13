package com.sickmartian.quickalarmwidget.data.model;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;

import com.sickmartian.quickalarmwidget.QAWApp;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
@Parcel
public class Alarm {

    // DB
    public static final String TABLE_NAME = "alarms";
    public Alarm(Cursor cursor) {
        alarmTime = LocalDateTime.parse(cursor.getString(0));
    }

    public class Fields {
        public static final String ALARM_TIME = "alarmTime";
    }
    static String[] projection = {TABLE_NAME + "." + Fields.ALARM_TIME};

    // Content Provider
    public static final String PATH = "alarms";
    public static final String CONTENT_AUTHORITY = "com.sickmartian.quickalarmwidget";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();
    public class Parameters {
        public static final String ALARM_FROM = "alarmFrom";
        public static final String ALARM_TO = "alarmTo";
        public static final String ALARM_TIME = "alarmTime";
    }


    public static Uri getAlarmById(int id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static Uri getAlarmByTime(String time) {
        return CONTENT_URI.buildUpon()
                .appendPath(Parameters.ALARM_TIME).appendPath(time.toString())
                .build();
    }

    public static Uri getAlarmBetweenTimes(LocalDateTime timeFrom, LocalDateTime timeTo) {
        return CONTENT_URI.buildUpon()
                .appendPath(Parameters.ALARM_FROM).appendPath(timeFrom.toString())
                .appendPath(Parameters.ALARM_TO).appendPath(timeTo.toString())
                .build();
    }

    // Data
    Alarm() {}
    LocalDateTime alarmTime;
    public LocalDateTime getAlarmTime() {
        return alarmTime;
    }
    public void setAlarmTime(LocalDateTime alarmTime) {
        this.alarmTime = alarmTime;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "alarmTime=" + alarmTime +
                '}';
    }

    // Get Sync
    public static List<Alarm> getBetweenDates(LocalDateTime from, LocalDateTime to) {
        ArrayList<Alarm> result = new ArrayList<Alarm>();
        final long token = Binder.clearCallingIdentity();
        try {
            Cursor cursor = QAWApp.getAppContext().getContentResolver()
                    .query(getAlarmBetweenTimes(from, to), projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        result.add(new Alarm(cursor));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        return result;
    }
}
