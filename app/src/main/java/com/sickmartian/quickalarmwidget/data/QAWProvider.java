package com.sickmartian.quickalarmwidget.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;

import java.util.List;

import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class QAWProvider extends ContentProvider {

    private static final int ALARM_BY_TIME = 1;
    private static final int ALARMS_BETWEEN_TIMES = 2;
    private QAWDBHelper DBHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        String alarmByTime = Alarm.PATH
                + "/" + Alarm.Parameters.ALARM_TIME + "/*";
        uriMatcher.addURI(Alarm.CONTENT_AUTHORITY, alarmByTime, ALARM_BY_TIME);

        String dateBoundAlarms = Alarm.PATH
                + "/" + Alarm.Parameters.ALARM_FROM + "/*"
                + "/" + Alarm.Parameters.ALARM_TO + "/*";
        uriMatcher.addURI(Alarm.CONTENT_AUTHORITY, dateBoundAlarms, ALARMS_BETWEEN_TIMES);

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
                        "datetime(" + Alarm.Fields.ALARM_TIME + ") >= datetime(?) AND " +
                        "datetime(" + Alarm.Fields.ALARM_TIME + ") <= datetime(?) ",
                        new String[]{dateFrom, dateTo},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: {
                String exceptionMessage = "Unknown uri: " + uri;
                Timber.e(exceptionMessage);
                throw new UnsupportedOperationException(exceptionMessage);
            }
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
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
                long id = db.insert(Alarm.TABLE_NAME, null, values);
                if ( id > 0 ) {
                    newUri = Alarm.getAlarmById((int) id);
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
                String time = uri.getPathSegments().get(1);
                rowsUpdated = db.delete(Alarm.TABLE_NAME,
                        "datetime(" + Alarm.Fields.ALARM_TIME + ") >= datetime(?)",
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
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
