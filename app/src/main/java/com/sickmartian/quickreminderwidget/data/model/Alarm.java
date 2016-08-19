package com.sickmartian.quickreminderwidget.data.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;

import com.sickmartian.quickreminderwidget.QRWApp;
import com.sickmartian.quickreminderwidget.Utils;

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
        dateTime = LocalDateTime.parse(cursor.getString(0));
        creationDateTime =LocalDateTime.parse(cursor.getString(1));
        note = cursor.getString(2);
        sourceWidgetId = cursor.getInt(3);
    }

    public class Fields {
        public static final String ALARM_DATE_TIME = "dateTime";
        public static final String ALARM_NOTE= "alarmNote";
        public static final String ALARM_CREATION_DATE_TIME = "creationDateTime";
        public static final String SOURCE_WIDGET_ID = "sourceWidgetId";
    }
    static String[] projection = {
            TABLE_NAME + "." + Fields.ALARM_DATE_TIME,
            TABLE_NAME + "." + Fields.ALARM_CREATION_DATE_TIME,
            TABLE_NAME + "." + Fields.ALARM_NOTE,
            TABLE_NAME + "." + Fields.SOURCE_WIDGET_ID
    };

    // Content Provider
    public static final String BASE_PATH = "alarms";
    public static final String NEXT_ALARM_PATH = "nextAlarm";
    public static final String NEXT_ALARMS_PATH = "nextAlarms";
    public static final String LAST_ALARMS_PATH = "lastAlarms";
    public static final String CONTENT_AUTHORITY = "com.sickmartian.quickreminderwidget";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(BASE_PATH).build();
    public static final Uri NEXT_ALARM_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(NEXT_ALARM_PATH).build();
    public static final Uri NEXT_ALARMS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(NEXT_ALARMS_PATH).build();
    public static final Uri LAST_ALARMS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(LAST_ALARMS_PATH).build();
    public class Parameters {
        public static final String ALARM_FROM = "alarmFrom";
        public static final String ALARM_TO = "alarmTo";
        public static final String ALARM_TIME = "dateTime";
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

    public static Uri getNextAlarms(LocalDateTime now) {
        return NEXT_ALARMS_CONTENT_URI.buildUpon()
                .appendPath(Parameters.ALARM_FROM).appendPath(now.toString())
                .build();
    }

    public static Uri getLatestsAlarms(LocalDateTime now) {
        return LAST_ALARMS_CONTENT_URI.buildUpon()
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
    LocalDateTime dateTime;
    LocalDateTime creationDateTime;
    String note;
    int sourceWidgetId;
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }
    public int getSourceWidgetId() {
        return sourceWidgetId;
    }
    public void setSourceWidgetId(int sourceWidgetId) {
        this.sourceWidgetId = sourceWidgetId;
    }

    public boolean isCustomDateTime(boolean every30) {
        if (every30) {
            return dateTime.getMinuteOfHour() != 0 && dateTime.getMinuteOfHour() != 30;
        } else {
            return dateTime.getMinuteOfHour() != 0;
        }
    }
    public static Alarm fromDateTime(LocalDateTime localDateTime) {
        Alarm alarm = new Alarm();
        alarm.setDateTime(localDateTime);
        return alarm;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "dateTime=" + dateTime +
                ", creationDateTime=" + creationDateTime +
                ", note='" + note + '\'' +
                ", sourceWidgetId=" + sourceWidgetId +
                '}';
    }

    // Sync Ops
    public static List<Alarm> getBetweenDatesSync(LocalDateTime from, LocalDateTime to) {
        ArrayList<Alarm> result = new ArrayList<Alarm>();
        final long token = Binder.clearCallingIdentity();
        try {
            Cursor cursor = QRWApp.getAppContext().getContentResolver()
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
            Cursor cursor = QRWApp.getAppContext().getContentResolver()
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

    public static List<Alarm> getNextsSync(LocalDateTime now) {
        ArrayList<Alarm> result = new ArrayList<Alarm>();
        final long token = Binder.clearCallingIdentity();
        try {
            Cursor cursor = QRWApp.getAppContext().getContentResolver()
                    .query(getNextAlarms(now), projection, null, null, null);
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

    public static List<Alarm> getLastsSync(LocalDateTime now) {
        ArrayList<Alarm> result = new ArrayList<Alarm>();
        final long token = Binder.clearCallingIdentity();
        try {
            Cursor cursor = QRWApp.getAppContext().getContentResolver()
                    .query(getLatestsAlarms(now), projection, null, null, null);
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

    public boolean createSync() {
        Uri newUri = null;
        final long token = Binder.clearCallingIdentity();
        try {
            newUri = QRWApp.getAppContext().getContentResolver()
                    .insert(getAlarmByTime(dateTime), getContentValues());
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        return newUri != null;
    }

    public void modifySync() {
        final long token = Binder.clearCallingIdentity();
        try {
            QRWApp.getAppContext().getContentResolver()
                    .update(getAlarmByTime(dateTime), getContentValues(), null, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Fields.ALARM_DATE_TIME, dateTime.toString());
        contentValues.put(Fields.ALARM_NOTE, note);
        if (creationDateTime == null) {
            creationDateTime = Utils.getNow();
        }
        contentValues.put(Fields.ALARM_CREATION_DATE_TIME, creationDateTime.toString());
        contentValues.put(Fields.SOURCE_WIDGET_ID, sourceWidgetId);
        return contentValues;
    }

    public void deleteSync(LocalDateTime localDateTime) {
        final long token = Binder.clearCallingIdentity();
        try {
            QRWApp.getAppContext().getContentResolver()
                    .delete(getAlarmByTime(localDateTime), null, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void deleteSync() {
        deleteSync(dateTime);
    }

    public static void deleteUpTo(LocalDateTime alarmTime) {
        final long token = Binder.clearCallingIdentity();
        try {
            QRWApp.getAppContext().getContentResolver()
                    .delete(getAlarmsUpTo(alarmTime), null, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
