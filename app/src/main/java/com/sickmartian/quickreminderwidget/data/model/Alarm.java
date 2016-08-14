package com.sickmartian.quickreminderwidget.data.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;

import com.sickmartian.quickreminderwidget.QAWApp;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;

import java.util.ArrayList;
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
    public static final String BASE_PATH = "alarms";
    public static final String NEXT_ALARM_PATH = "nextAlarm";
    public static final String LAST_ALARM_PATH = "lastAlarm";
    public static final String CONTENT_AUTHORITY = "com.sickmartian.quickreminderwidget";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(BASE_PATH).build();
    public static final Uri NEXT_ALARM_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(NEXT_ALARM_PATH).build();
    public static final Uri LAST_ALARM_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(LAST_ALARM_PATH).build();
    public class Parameters {
        public static final String ALARM_FROM = "alarmFrom";
        public static final String ALARM_TO = "alarmTo";
        public static final String ALARM_TIME = "alarmTime";
    }


    public static Uri getAlarmById(int id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static Uri getAlarmByTime(LocalDateTime time) {
        return CONTENT_URI.buildUpon()
                .appendPath(Parameters.ALARM_TIME).appendPath(time.toString())
                .build();
    }

    public static Uri getNextAlarm(LocalDateTime now) {
        return NEXT_ALARM_CONTENT_URI.buildUpon()
                .appendPath(Parameters.ALARM_FROM).appendPath(now.toString())
                .build();
    }

    public static Uri getLastAlarm(LocalDateTime now) {
        return LAST_ALARM_CONTENT_URI.buildUpon()
                .appendPath(Parameters.ALARM_TO).appendPath(now.toString())
                .build();
    }

    public static Uri getAlarmBetweenTimes(LocalDateTime timeFrom, LocalDateTime timeTo) {
        return CONTENT_URI.buildUpon()
                .appendPath(Parameters.ALARM_FROM).appendPath(timeFrom.toString())
                .appendPath(Parameters.ALARM_TO).appendPath(timeTo.toString())
                .build();
    }

    public static Uri getAlarmsUpTo(LocalDateTime timeTo) {
        return CONTENT_URI.buildUpon()
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
    public boolean isCustomTime(boolean every30) {
        if (every30) {
            return alarmTime.getMinuteOfHour() != 0 && alarmTime.getMinuteOfHour() != 30;
        } else {
            return alarmTime.getMinuteOfHour() != 0;
        }
    }
    public static Alarm fromTime(LocalDateTime localDateTime) {
        Alarm alarm = new Alarm();
        alarm.setAlarmTime(localDateTime);
        return alarm;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "alarmTime=" + alarmTime +
                '}';
    }

    // Sync Ops
    public static List<Alarm> getBetweenDatesSync(LocalDateTime from, LocalDateTime to) {
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

    public static Alarm getNextSync(LocalDateTime now) {
        Alarm alarm = null;
        final long token = Binder.clearCallingIdentity();
        try {
            Cursor cursor = QAWApp.getAppContext().getContentResolver()
                    .query(getNextAlarm(now), projection, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        alarm = new Alarm(cursor);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        return alarm;
    }

    public static Alarm getLastSync(LocalDateTime now) {
        Alarm alarm = null;
        final long token = Binder.clearCallingIdentity();
        try {
            Cursor cursor = QAWApp.getAppContext().getContentResolver()
                    .query(getLastAlarm(now), projection, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        alarm = new Alarm(cursor);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        return alarm;
    }

    public void saveSync() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Fields.ALARM_TIME, alarmTime.toString());
        final long token = Binder.clearCallingIdentity();
        try {
            QAWApp.getAppContext().getContentResolver()
                    .insert(getAlarmByTime(alarmTime), contentValues);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void deleteSync() {
        final long token = Binder.clearCallingIdentity();
        try {
            QAWApp.getAppContext().getContentResolver()
                    .delete(getAlarmByTime(alarmTime), null, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static void deleteUpTo(LocalDateTime alarmTime) {
        final long token = Binder.clearCallingIdentity();
        try {
            QAWApp.getAppContext().getContentResolver()
                    .delete(getAlarmsUpTo(alarmTime), null, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
