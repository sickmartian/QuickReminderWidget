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

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickAlarmWidgetService extends RemoteViewsService {

    boolean every30 = true;
    int hours = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuickAlarmWidgetViewFactory(getApplicationContext());
    }

    public final class QuickAlarmWidgetViewFactory implements RemoteViewsFactory {
        private final Context mAppContext;
        LocalDateTime initialTime;
        int rows;
        DateTimeFormatter timeFormatter;

        public QuickAlarmWidgetViewFactory(Context applicationContext) {
            JodaTimeAndroid.init(applicationContext);

            mAppContext = applicationContext;
        }

        @Override
        public void onCreate() {
            timeFormatter = DateTimeFormat.shortTime();
            LocalDateTime dateTime = LocalDateTime.now();
            int minutes = dateTime.getMinuteOfHour();

            rows = hours;
            if (every30) {
                rows *= 2;
            }
            if (minutes > 30) {
                initialTime = dateTime.minusMinutes(dateTime.getMinuteOfHour() - 30);
                rows--;
            } else {
                initialTime = dateTime.minusMinutes(dateTime.getMinuteOfHour());
            }
        }

        @Override
        public void onDataSetChanged() {

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
            RemoteViews itemView = new RemoteViews(mAppContext.getPackageName(),
                    R.layout.quick_widget_item_layout);
            LocalDateTime time;
            if (i > 0) {
                time = initialTime.plusMinutes(30 * i);
            } else {
                time = initialTime;
            }
            itemView.setTextViewText(R.id.item_text, time.toString(timeFormatter));
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
