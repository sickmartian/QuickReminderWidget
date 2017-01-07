package com.sickmartian.quickreminderwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        boolean editionMode;

        private int firstTimeRow;
        private LocalDateTime initialTime;
        private List<ReminderIntentionData> reminderIntentionData;

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

            reminderIntentionData = new ArrayList<ReminderIntentionData>();
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

            reminderIntentionData.clear();
            // We need to get the editionMode here, as this can be called before
            // a new Factory is created with the updated value
            editionMode = App.getWidgetSharedPref(appWidgetId)
                    .getBoolean(QuickReminderWidgetProvider.EDITION_MODE, false);

            initialTime = App.getInitialTime(every30);
            LocalDateTime endTime = initialTime.plusHours(hours);
            if (every30) {
                rowsForHour = hours * 2 + 1;
            } else {
                rowsForHour = hours + 1;
            }

            firstTimeRow = 0;
            if (!editionMode) {
                // Add rows for custom values first (10 minutes from now, 30 minutes from now, etc)
                if (customValue1 > 0) {
                    firstTimeRow++;
                    reminderIntentionData.add(new ReminderIntentionData(Duration.standardMinutes(customValue1), null));
                }
                if (customValue2 > 0) {
                    firstTimeRow++;
                    reminderIntentionData.add(new ReminderIntentionData(Duration.standardMinutes(customValue2), null));
                }
                if (customValue3 > 0) {
                    firstTimeRow++;
                    reminderIntentionData.add(new ReminderIntentionData(Duration.standardMinutes(customValue3), null));
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
                            reminderIntentionData.add(new ReminderIntentionData(timeForTimeRow, alarm));
                            alarmWeLeftOff = alarmIndex + 1;
                            timeWasAdded = true;
                            break;
                        } else if (alarm.getDateTime().isBefore(timeForTimeRow)) {
                            // Alarm set via custom value
                            reminderIntentionData.add(new ReminderIntentionData(alarm.getDateTime(), alarm));
                            alarmWeLeftOff = alarmIndex + 1;
                        } else {
                            // This alarm will be set later
                            break;
                        }
                    }
                    if (!timeWasAdded) {
                        reminderIntentionData.add(new ReminderIntentionData(timeForTimeRow, null));
                    }
                }
            } else {
                LocalDateTime alarmBase = Utils.getNow()
                        .plusMinutes(1)
                        .withSecondOfMinute(0)
                        .withMillisOfSecond(0);
                alarms = Alarm.getNextsSync(alarmBase);
                for (Alarm alarm : alarms) {
                    reminderIntentionData.add(new ReminderIntentionData(alarm.getDateTime(), alarm));
                }
            }
        }

        public LocalDateTime getTimeForTimeRow(int timeRow) {
            // If we are over the custom values
            if (timeRow > 0) {
                // Return the time
                if (every30) {
                    return initialTime.plusMinutes(App.HALF_HOUR_MINUTES * (timeRow));
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
            return reminderIntentionData.size();
        }

        @Override
        public RemoteViews getViewAt(int row) {
            RemoteViews itemView = new RemoteViews(App.getAppContext().getPackageName(),
                    R.layout.quick_widget_item_layout);

            ReminderIntentionData currentReminderIntentionData = reminderIntentionData.get(row);

            if (currentReminderIntentionData.getTime() != null) {
                itemView.setTextViewText(R.id.item_text,
                        currentReminderIntentionData.getTime().toString(App.timeFormatter));
                if (currentReminderIntentionData.getAlarm() != null) {
                    itemView.setTextColor(R.id.item_text, App.activeColor);
                    if (currentReminderIntentionData.getAlarm().getNote() != null) {
                        itemView.setInt(R.id.note_link, "setColorFilter", App.activeColor);
                    } else {
                        itemView.setInt(R.id.note_link, "setColorFilter", App.inactiveColor);
                    }
                } else {
                    itemView.setTextColor(R.id.item_text, App.inactiveColor);
                    itemView.setInt(R.id.note_link, "setColorFilter", App.inactiveColor);
                }
                if (possibilityToAddNote) {
                    itemView.setViewVisibility(R.id.note_link, View.VISIBLE);
                } else {
                    itemView.setViewVisibility(R.id.note_link, View.GONE);
                }
                // If we have a row before this one, that is a time row
                // and that date is not the same, or
                if (row > firstTimeRow && reminderIntentionData.get(row - 1).getTime() != null &&
                        !reminderIntentionData.get(row - 1).getTime().toLocalDate().equals(
                                currentReminderIntentionData.getTime().toLocalDate()) ||
                        // If it's the first row
                        row == firstTimeRow) {
                    itemView.setViewVisibility(R.id.date_row, View.VISIBLE);
                    itemView.setTextViewText(R.id.date_row, currentReminderIntentionData.getTime().toString(App.dateFormatter));
                } else {
                    itemView.setViewVisibility(R.id.date_row, View.GONE);
                }
            } else if (currentReminderIntentionData.getDuration() != null) {
                itemView.setTextViewText(R.id.item_text,
                        CustomAlarmTimeValue.getCustomValueShortLabel((int) currentReminderIntentionData.getDuration().getStandardMinutes()));
                itemView.setTextColor(R.id.item_text, App.inactiveColor);
                itemView.setViewVisibility(R.id.note_link, View.GONE);
                itemView.setViewVisibility(R.id.date_row, View.GONE);
            }

            if (!editionMode) {
                itemView.setImageViewResource(R.id.note_link, R.drawable.ic_event_note_black_24dp);

                // Fill each alarm's intention to the intents
                Intent intent = new Intent();
                Bundle extras = new Bundle();
                extras.putParcelable(ReminderIntentionReceiver.ALARM_INTENTION_DATA,
                        Parcels.wrap(currentReminderIntentionData));
                extras.putBoolean(ReminderIntentionReceiver.AND_OFFER_EDITION, possibilityToAddNote);
                extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtras(extras);
                itemView.setOnClickFillInIntent(R.id.clickeable_row,
                        intent);
            } else {
                itemView.setImageViewResource(R.id.note_link, R.drawable.ic_create_black_24dp);

                // Fill each edition intention to the intents
                itemView.setOnClickFillInIntent(R.id.clickeable_row,
                        ReminderEditionActivity.getIntentForEditionPart2(currentReminderIntentionData.getAlarm(),
                                appWidgetId));
            }

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
