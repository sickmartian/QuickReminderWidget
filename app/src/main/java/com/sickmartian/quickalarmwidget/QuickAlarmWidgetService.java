package com.sickmartian.quickalarmwidget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import java.io.Serializable;
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

        int rows;
        private LocalDateTime initialTime;
        private List<Alarm> alarms;
        private int customValues;

        public QuickAlarmWidgetViewFactory(boolean every30, int hours,
                                           int customValue1, int customValue2, int customValue3) {
            this.every30 = every30;
            this.hours = hours;
            this.customValue1 = customValue1;
            this.customValue2 = customValue2;
            this.customValue3 = customValue3;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            Timber.d("QAWS onDataSetChanged");
            calculateInternalState();
        }

        public void calculateInternalState() {
            initialTime = QAWApp.getInitialTime(every30);
            LocalDateTime endTime = initialTime.plusHours(hours);
            if (every30) {
                rows = hours * 2 + 1;
            } else {
                rows = hours + 1;
            }

            // Add rows for custom values (10 minutes from now, 30 minutes from now, etc)
            customValues = 0;
            if (customValue1 > 0) {
                rows++;
                customValues++;
            }
            if (customValue2 > 0) {
                rows++;
                customValues++;
            }
            if (customValue3 > 0) {
                rows++;
                customValues++;
            }

            Timber.d("now: " + Utils.getNow().toString());
            Timber.d("initialTime: " + initialTime.toString());
            Timber.d("endTime: " + endTime.toString());

            alarms = Alarm.getBetweenDatesSync(initialTime, endTime);
            // Add rows for alarms set for the custom values
            for (Alarm alarm : alarms) {
                int minutes = alarm.getAlarmTime().getMinuteOfHour();
                if (minutes != 0 && minutes != 30) {
                    rows++;
                }
            }
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return rows;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews itemView = new RemoteViews(QAWApp.getAppContext().getPackageName(),
                    R.layout.quick_widget_item_layout);

            Serializable timeObjectForRow = getTimeObjectForRow(i);

            Bundle extras = new Bundle();
            if (timeObjectForRow instanceof LocalDateTime) {
                LocalDateTime timeForRow = (LocalDateTime) timeObjectForRow;
                Alarm alarm = getAlarmForTime(timeForRow);
                extras.putParcelable(AlarmIntentionReceiver.ALARM, Parcels.wrap(alarm));

                itemView.setTextViewText(R.id.item_text, timeForRow.toString(QAWApp.timeFormatter));
                if (alarm != null) {
                    itemView.setTextColor(R.id.item_text, getColor(R.color.colorAccent));
                } else {
                    itemView.setTextColor(R.id.item_text, getColor(android.R.color.white));
                }
            } else if (timeObjectForRow instanceof Duration) {
                Duration duration = (Duration) timeObjectForRow;
                itemView.setTextViewText(R.id.item_text, Long.toString(duration.getStandardMinutes()) + "\"");
            }

            Intent intent = new Intent();
            extras.putSerializable(AlarmIntentionReceiver.ALARM_TIME_OBJECT, timeObjectForRow);
            intent.putExtras(extras);
            itemView.setOnClickFillInIntent(R.id.clickeable_row, intent);

            return itemView;
        }

        public Serializable getTimeObjectForRow(int row) {
            // Return custom durations (assumes customValue2 is never filled
            // without customValue1 and so on)
            if (row == 0 && customValues > 0) {
                return Duration.standardMinutes(customValue1);
            }
            if (row == 1 && customValues > 1) {
                return Duration.standardMinutes(customValue2);
            }
            if (row == 2 && customValues > 2) {
                return Duration.standardMinutes(customValue3);
            }

            // If we are over the custom values
            if (row > customValues) {
                // Return the time
                if (every30) {
                    return initialTime.plusMinutes(QAWApp.HALF_HOUR_MINUTES * (row - customValues));
                } else {
                    return initialTime.plusHours(row - customValues);
                }
            } else {
                // Return the initial time
                return initialTime;
            }
        }

        private Alarm getAlarmForTime(LocalDateTime timeForRow) {
            for (Alarm alarm : alarms) {
                if (alarm.getAlarmTime().equals(timeForRow)) {
                    return alarm;
                }
            }
            return null;
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
