package com.sickmartian.quickreminderwidget.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import java.util.List;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class QAWProvider extends ContentProvider {

    private static final int ALARM_BY_TIME = 1;
    private static final int ALARMS_BETWEEN_TIMES = 2;
    private static final int NEXT_ALARM = 3;
    private static final int LAST_ALARM = 4;
    private static final int ALARMS_UP_TO = 5;
    private QAWDBHelper DBHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        String alarmByTime = Alarm.BASE_PATH
                + "/" + Alarm.Parameters.ALARM_TIME + "/*";
        uriMatcher.addURI(Alarm.CONTENT_AUTHORITY, alarmByTime, ALARM_BY_TIME);

        String alarmsUpTo = Alarm.BASE_PATH
                + "/" + Alarm.Parameters.ALARM_TO + "/*";
        uriMatcher.addURI(Alarm.CONTENT_AUTHORITY, alarmsUpTo, ALARMS_UP_TO);

        String dateBoundAlarms = Alarm.BASE_PATH
                + "/" + Alarm.Parameters.ALARM_FROM + "/*"
                + "/" + Alarm.Parameters.ALARM_TO + "/*";
        uriMatcher.addURI(Alarm.CONTENT_AUTHORITY, dateBoundAlarms, ALARMS_BETWEEN_TIMES);

        String nextAlarm = Alarm.NEXT_ALARM_PATH
                + "/" + Alarm.Parameters.ALARM_FROM + "/*";
        uriMatcher.addURI(Alarm.CONTENT_AUTHORITY, nextAlarm, NEXT_ALARM);

        String lastAlarm = Alarm.LAST_ALARMS_PATH
                + "/" + Alarm.Parameters.ALARM_TO + "/*";
        uriMatcher.addURI(Alarm.CONTENT_AUTHORITY, lastAlarm, LAST_ALARM);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        DBHelper = new QAWDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = DBHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);

        Cursor cursor;
        switch (match) {
            case ALARMS_BETWEEN_TIMES: {
                List<String> segments = uri.getPathSegments();
                String dateFrom = segments.get(2);
                String dateTo = segments.get(4);
                cursor = db.query(Alarm.TABLE_NAME,
                        projection,
                        "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") >= datetime(?) AND " +
                        "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") <= datetime(?) ",
                        new String[]{dateFrom, dateTo},
                        null,
                        null,
                        sortOrder == null ?
                                Alarm.TABLE_NAME + "." + Alarm.Fields.ALARM_DATE_TIME + " asc" :
                                sortOrder);
                break;
            }
            case NEXT_ALARM: {
                List<String> segments = uri.getPathSegments();
                String dateFrom = segments.get(2);
                cursor = db.query(Alarm.TABLE_NAME,
                        new String[]{"MIN( " + Alarm.Fields.ALARM_DATE_TIME + " ) "},
                        "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") > datetime(?) ",
                        new String[]{dateFrom},
                        null,
                        null,
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String alarmTime = cursor.getString(0);
                    if (alarmTime == null) {
                        cursor = null;
                    } else {
                        cursor = db.query(Alarm.TABLE_NAME,
                            projection,
                            "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") = datetime(?) ",
                            new String[]{alarmTime},
                            null,
                            null,
                            null);
                    }
                }
                break;
            }
            case LAST_ALARM: {
                List<String> segments = uri.getPathSegments();
                String dateTo = segments.get(2);
                cursor = db.query(Alarm.TABLE_NAME,
                        projection,
                        "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") <= datetime(?) ",
                        new String[]{dateTo},
                        null,
                        null,
                        null);
                break;
            }
            default: {
                String exceptionMessage = "Unknown uri: " + uri;
                Timber.e(exceptionMessage);
                throw new UnsupportedOperationException(exceptionMessage);
            }
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = DBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri newUri = null;

        switch (match) {
            case ALARM_BY_TIME: {
                try {
                    long id = db.insert(Alarm.TABLE_NAME, null, values);
                    if ( id > 0 ) {
                        newUri = Alarm.getAlarmById((int) id);
                    } else {
                        Timber.i("Tried to add an existing alarm! Ignoring...");
                    }
                } catch (SQLiteConstraintException e) {
                    Timber.i("Tried to add an existing alarm! Ignoring...");
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = DBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ALARM_BY_TIME: {
                String time = uri.getPathSegments().get(2);
                rowsUpdated = db.delete(Alarm.TABLE_NAME,
                        "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") = datetime(?)",
                        new String[]{time});
                break;
            }
            case ALARMS_UP_TO: {
                String time = uri.getPathSegments().get(2);
                rowsUpdated = db.delete(Alarm.TABLE_NAME,
                        "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") <= datetime(?)",
                        new String[]{time});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = DBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ALARM_BY_TIME: {
                String time = uri.getPathSegments().get(2);
                rowsUpdated = db.update(Alarm.TABLE_NAME, values,
                        "datetime(" + Alarm.Fields.ALARM_DATE_TIME + ") = datetime(?)",
                        new String[]{time});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
