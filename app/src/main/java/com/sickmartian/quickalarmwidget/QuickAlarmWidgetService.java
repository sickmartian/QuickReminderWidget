package com.sickmartian.quickalarmwidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickAlarmWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuickAlarmWidgetViewFactory(getApplicationContext());
    }

    public final class QuickAlarmWidgetViewFactory implements RemoteViewsFactory {
        private final Context mAppContext;

        public QuickAlarmWidgetViewFactory(Context applicationContext) {
            mAppContext = applicationContext;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            return new RemoteViews(mAppContext.getPackageName(),
                    R.layout.quick_widget_item_layout);
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
