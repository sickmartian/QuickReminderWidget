package com.sickmartian.quickalarmwidget.model;

import org.joda.time.LocalDateTime;

import io.realm.RealmObject;

/**
 * Created by ***REMOVED*** on 8/12/16.
 */
public class Alarm extends RealmObject {
    String alarmLocalTime;

    public LocalDateTime getAlarmLocalTime() {
        return LocalDateTime.parse(alarmLocalTime);
    }

    public void setAlarmLocalTime(LocalDateTime alarmLocalTime) {
        this.alarmLocalTime = alarmLocalTime.toString();
    }
}
