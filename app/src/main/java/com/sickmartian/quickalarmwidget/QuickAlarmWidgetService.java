package com.sickmartian.quickalarmwidget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sickmartian.quickalarmwidget.data.model.Alarm;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import java.util.List;
import timber.log.Timber;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickAlarmWidgetService extends RemoteViewsService {
    int hours = 4;
    boolean every30 = true;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuickAlarmWidgetViewFactory();
    }

    public final class QuickAlarmWidgetViewFactory implements RemoteViewsFactory {
        int rows;
        private LocalDateTime initialTime;
        private List<Alarm> alarms;

        public QuickAlarmWidgetViewFactory() {

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

            Timber.d("now: " + Utils.getNow().toString());
            Timber.d("initialTime: " + initialTime.toString());
            Timber.d("endTime: " + endTime.toString());

            alarms = Alarm.getBetweenDatesSync(initialTime, endTime);
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

            LocalDateTime timeForRow = getTimeForRow(i);

            itemView.setTextViewText(R.id.item_text, timeForRow.toString(QAWApp.timeFormatter));
            Alarm alarm = getAlarmForTime(timeForRow);
            if (alarm != null) {
                itemView.setTextColor(R.id.item_text, getColor(R.color.colorAccent));
            } else {
                itemView.setTextColor(R.id.item_text, getColor(android.R.color.white));
            }

            Intent intent = new Intent();
            Bundle extras = new Bundle();

            extras.putSerializable(AlarmIntentionReceiver.ALARM_TIME, timeForRow);
            extras.putParcelable(AlarmIntentionReceiver.ALARM, Parcels.wrap(alarm));
            intent.putExtras(extras);
            itemView.setOnClickFillInIntent(R.id.clickeable_row, intent);

            return itemView;
        }

        private Alarm getAlarmForTime(LocalDateTime timeForRow) {
            for (Alarm alarm : alarms) {
                if (alarm.getAlarmTime().equals(timeForRow)) {
                    return alarm;
                }
            }
            return null;
        }

        public LocalDateTime getTimeForRow(int row) {
            if (row > 0) {
                if (every30) {
                    return initialTime.plusMinutes(QAWApp.HALF_HOUR_MINUTES * row);
                } else {
                    return initialTime.plusHours(row);
                }
            } else {
                return initialTime;
            }
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
