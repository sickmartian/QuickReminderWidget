package com.sickmartian.quickalarmwidget.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class QAWDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "qaw.db";
    public static final int CURRENT_VERSION = 1;
    public QAWDBHelper(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");

        final String SQL_CREATE_ALARM_TABLE = "CREATE TABLE " + Alarm.TABLE_NAME + " (" +
                Alarm.Fields.ALARM_TIME + " TEXT NOT NULL UNIQUE );";

        db.execSQL(SQL_CREATE_ALARM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
