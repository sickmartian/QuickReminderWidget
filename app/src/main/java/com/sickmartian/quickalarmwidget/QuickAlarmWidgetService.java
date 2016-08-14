package com.sickmartian.quickalarmwidget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.parceler.Parcel;
import org.parceler.Parcels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickAlarmWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuickAlarmWidgetViewFactory(
                intent.getBooleanExtra(QuickAlarmWidgetProvider.EVERY_30, true),
                intent.getIntExtra(QuickAlarmWidgetProvider.HOURS, 4),
                intent.getIntExtra(QuickAlarmWidgetProvider.CUSTOM_TIME_1, -1),
                intent.getIntExtra(QuickAlarmWidgetProvider.CUSTOM_TIME_2, -1),
                intent.getIntExtra(QuickAlarmWidgetProvider.CUSTOM_TIME_3, -1));
    }

    public final class QuickAlarmWidgetViewFactory implements RemoteViewsFactory {
        int hours;
        boolean every30;
        int customValue1;
        int customValue2;
        int customValue3;

        private LocalDateTime initialTime;
        private List<AlarmIntentionData> alarmIntentionData;

        public QuickAlarmWidgetViewFactory(boolean every30, int hours,
                                           int customValue1, int customValue2, int customValue3) {
            this.every30 = every30;
            this.hours = hours;
            this.customValue1 = customValue1;
            this.customValue2 = customValue2;
            this.customValue3 = customValue3;

            alarmIntentionData = new ArrayList<AlarmIntentionData>();
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            calculateInternalState();
        }

        public void calculateInternalState() {
            Timber.d("Calculating Widget Initial State");
            int rowsForHour;
            List<Alarm> alarms;

            alarmIntentionData.clear();

            initialTime = QAWApp.getInitialTime(every30);
            LocalDateTime endTime = initialTime.plusHours(hours);
            if (every30) {
                rowsForHour = hours * 2 + 1;
            } else {
                rowsForHour = hours + 1;
            }

            // Add rows for custom values first (10 minutes from now, 30 minutes from now, etc)
            if (customValue1 > 0) {
                alarmIntentionData.add(new AlarmIntentionData(Duration.standardMinutes(customValue1), null));
            }
            if (customValue2 > 0) {
                alarmIntentionData.add(new AlarmIntentionData(Duration.standardMinutes(customValue2), null));
            }
            if (customValue3 > 0) {
                alarmIntentionData.add(new AlarmIntentionData(Duration.standardMinutes(customValue3), null));
            }

            LocalDateTime now = Utils.getNow();
            alarms = Alarm.getBetweenDatesSync(now, endTime);
            int alarmWeLeftOff = 0;
            int alarmIndex;
            // Normal times and custom set alarms
            // (There shouldn't be alarms after the normal times, as we are
            // filtering by those times)
            for (int timeRowIndex = 0; timeRowIndex < rowsForHour; timeRowIndex++) {
                LocalDateTime timeForTimeRow = getTimeForTimeRow(timeRowIndex);
                boolean timeWasAdded = false;
                for (alarmIndex = alarmWeLeftOff; alarmIndex < alarms.size(); alarmIndex++) {
                    alarmWeLeftOff = alarmIndex;
                    Alarm alarm = alarms.get(alarmIndex);
                    if (alarm.getAlarmTime().isEqual(timeForTimeRow)) {
                        // Normally set alarm
                        alarmIntentionData.add(new AlarmIntentionData(timeForTimeRow, alarm));
                        alarmWeLeftOff = alarmIndex + 1;
                        timeWasAdded = true;
                        break;
                    } else if (alarm.getAlarmTime().isBefore(timeForTimeRow)) {
                        // Alarm set via custom value
                        alarmIntentionData.add(new AlarmIntentionData(alarm.getAlarmTime(), alarm));
                        alarmWeLeftOff = alarmIndex + 1;
                    } else {
                        // This alarm will be set later
                        break;
                    }
                }
                if (!timeWasAdded) {
                    alarmIntentionData.add(new AlarmIntentionData(timeForTimeRow, null));
                }
            }

            for (AlarmIntentionData aid : alarmIntentionData) {
                Timber.i(aid.toString());
            }
        }

        public LocalDateTime getTimeForTimeRow(int timeRow) {
            // If we are over the custom values
            if (timeRow > 0) {
                // Return the time
                if (every30) {
                    return initialTime.plusMinutes(QAWApp.HALF_HOUR_MINUTES * (timeRow));
                } else {
                    return initialTime.plusHours(timeRow );
                }
            } else {
                // Return the initial time
                return initialTime;
            }
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return alarmIntentionData.size();
        }

        @Override
        public RemoteViews getViewAt(int row) {
            RemoteViews itemView = new RemoteViews(QAWApp.getAppContext().getPackageName(),
                    R.layout.quick_widget_item_layout);

            AlarmIntentionData currentAlarmIntentionData = alarmIntentionData.get(row);

            Bundle extras = new Bundle();
            if (currentAlarmIntentionData.getTime() != null) {
                itemView.setTextViewText(R.id.item_text,
                        currentAlarmIntentionData.getTime().toString(QAWApp.timeFormatter));
                if (currentAlarmIntentionData.getAlarm() != null) {
                    itemView.setTextColor(R.id.item_text, getColor(R.color.colorAccent));
                } else {
                    itemView.setTextColor(R.id.item_text, getColor(android.R.color.white));
                }
            } else if (currentAlarmIntentionData.getDuration() != null) {
                itemView.setTextViewText(R.id.item_text,
                        Long.toString(currentAlarmIntentionData.getDuration().getStandardMinutes()) + "\"");
                itemView.setTextColor(R.id.item_text, getColor(android.R.color.white));
            }

            Intent intent = new Intent();
            extras.putParcelable(AlarmIntentionReceiver.ALARM_INTENTION_DATA,
                    Parcels.wrap(currentAlarmIntentionData));
            intent.putExtras(extras);
            itemView.setOnClickFillInIntent(R.id.clickeable_row, intent);

            return itemView;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
