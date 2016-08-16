package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sickmartian.quickreminderwidget.data.model.Alarm;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickReminderWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuickReminderWidgetViewFactory(
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                intent.getBooleanExtra(QuickReminderWidgetProvider.EVERY_30, true),
                intent.getIntExtra(QuickReminderWidgetProvider.HOURS, 4),
                intent.getBooleanExtra(QuickReminderWidgetProvider.POSSIBILITY_TO_ADD_NOTE, true),
                intent.getIntExtra(QuickReminderWidgetProvider.CUSTOM_TIME_1, -1),
                intent.getIntExtra(QuickReminderWidgetProvider.CUSTOM_TIME_2, -1),
                intent.getIntExtra(QuickReminderWidgetProvider.CUSTOM_TIME_3, -1));
    }

    public final class QuickReminderWidgetViewFactory implements RemoteViewsFactory {
        int appWidgetId;
        int hours;
        boolean every30;
        int customValue1;
        int customValue2;
        int customValue3;
        boolean possibilityToAddNote;

        private int firstTimeRow;
        private LocalDateTime initialTime;
        private List<AlarmIntentionData> alarmIntentionData;

        public QuickReminderWidgetViewFactory(int appWidgetId,
                                              boolean every30, int hours, boolean possibilityToAddNote,
                                              int customValue1, int customValue2, int customValue3) {
            this.appWidgetId = appWidgetId;
            this.every30 = every30;
            this.hours = hours;
            this.possibilityToAddNote = possibilityToAddNote;
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
            firstTimeRow = 0;
            if (customValue1 > 0) {
                firstTimeRow++;
                alarmIntentionData.add(new AlarmIntentionData(Duration.standardMinutes(customValue1), null));
            }
            if (customValue2 > 0) {
                firstTimeRow++;
                alarmIntentionData.add(new AlarmIntentionData(Duration.standardMinutes(customValue2), null));
            }
            if (customValue3 > 0) {
                firstTimeRow++;
                alarmIntentionData.add(new AlarmIntentionData(Duration.standardMinutes(customValue3), null));
            }

            // The base of the range for the alarms is set to 1 minute in the future
            // so in case that the TimeSync Service runs before the Notification Service
            // we don't get the alarm that the Notification Service is supposed to clear
            LocalDateTime alarmBase = Utils.getNow()
                    .plusMinutes(1)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0);
            alarms = Alarm.getBetweenDatesSync(alarmBase, endTime);
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
                    if (alarm.getDateTime().isEqual(timeForTimeRow)) {
                        // Normally set alarm
                        alarmIntentionData.add(new AlarmIntentionData(timeForTimeRow, alarm));
                        alarmWeLeftOff = alarmIndex + 1;
                        timeWasAdded = true;
                        break;
                    } else if (alarm.getDateTime().isBefore(timeForTimeRow)) {
                        // Alarm set via custom value
                        alarmIntentionData.add(new AlarmIntentionData(alarm.getDateTime(), alarm));
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
                    itemView.setTextColor(R.id.item_text, QAWApp.activeColor);
                    if (currentAlarmIntentionData.getAlarm().getNote() != null) {
                        itemView.setInt(R.id.note_link, "setColorFilter", QAWApp.activeColor);
                    } else {
                        itemView.setInt(R.id.note_link, "setColorFilter", QAWApp.inactiveColor);
                    }
                } else {
                    itemView.setTextColor(R.id.item_text, QAWApp.inactiveColor);
                    itemView.setInt(R.id.note_link, "setColorFilter", QAWApp.inactiveColor);
                }
                if (possibilityToAddNote) {
                    itemView.setViewVisibility(R.id.note_link, View.VISIBLE);
                } else {
                    itemView.setViewVisibility(R.id.note_link, View.GONE);
                }
                // First row and each row for new dates get the date printed
                if (currentAlarmIntentionData.getTime().getMillisOfDay() == 0 ||
                        row == firstTimeRow) {
                    itemView.setViewVisibility(R.id.date_row, View.VISIBLE);
                    itemView.setTextViewText(R.id.date_row, currentAlarmIntentionData.getTime().toString(QAWApp.dateFormatter));
                } else {
                    itemView.setViewVisibility(R.id.date_row, View.GONE);
                }
            } else if (currentAlarmIntentionData.getDuration() != null) {
                itemView.setTextViewText(R.id.item_text,
                        String.format(getString(R.string.custom_value_format),
                                Long.toString(currentAlarmIntentionData.getDuration().getStandardMinutes())));
                itemView.setTextColor(R.id.item_text, QAWApp.inactiveColor);
                itemView.setViewVisibility(R.id.note_link, View.GONE);
                itemView.setViewVisibility(R.id.date_row, View.GONE);
            }

            Intent intent = new Intent();
            extras.putParcelable(AlarmIntentionReceiver.ALARM_INTENTION_DATA,
                    Parcels.wrap(currentAlarmIntentionData));
            extras.putBoolean(AlarmIntentionReceiver.AND_OFFER_EDITION, possibilityToAddNote);
            extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtras(extras);
            itemView.setOnClickFillInIntent(R.id.clickeable_row,
                    intent);

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
