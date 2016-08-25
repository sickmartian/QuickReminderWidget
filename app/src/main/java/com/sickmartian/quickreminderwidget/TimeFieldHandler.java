package com.sickmartian.quickreminderwidget;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by sickmartian on 24/08/16.
 */
public class TimeFieldHandler implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    private LocalTime mCurrentValue;
    private final TextView mField;
    private final Context mContext;
    private boolean mIsDirty;

    public TimeFieldHandler(Context context, TextView field, LocalTime localTime) {
        mContext = context;
        mField = field;
        setAndShowNewDate(mField, localTime);
        mField.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        LocalTime localTime = getCurrentValue();
        Utils.getTimePickerDialog(mContext, this,
                localTime.getHourOfDay(), localTime.getMinuteOfHour(),
                android.text.format.DateFormat.is24HourFormat(mContext)).show();
    }

    public void setAndShowNewDate(TextView customStartDate, LocalTime localTime) {
        mCurrentValue = localTime;
        customStartDate.setText(localTime.toString(DateTimeFormat.shortTime()));
    }

    public boolean isDirty() {
        return mIsDirty;
    }

    public LocalTime getCurrentValue() {
        return mCurrentValue;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setAndShowNewDate(mField, new LocalTime(hourOfDay, minute));
        mIsDirty = true;
    }
}