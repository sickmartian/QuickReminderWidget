package com.sickmartian.quickalarmwidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
            itemView.setTextViewText(R.id.item_text, getTimeForRow(i).toString(QAWApp.timeFormatter));
            return itemView;
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
