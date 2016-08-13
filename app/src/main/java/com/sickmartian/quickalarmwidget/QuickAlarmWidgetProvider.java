package com.sickmartian.quickalarmwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickAlarmWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_IDS_KEY ="WIDGET_IDS_KEY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent svcIntent = new Intent(context, QuickAlarmWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.quick_widget_layout);

            widget.setRemoteAdapter(appWidgetId, R.id.quick_widget_list, svcIntent);
            Intent clickIntent = new Intent(context, AlarmIntentionReceiver.class);
            PendingIntent clickPI = PendingIntent.getBroadcast(context, 0,
                    clickIntent, 0);
            widget.setPendingIntentTemplate(R.id.quick_widget_list, clickPI);

            appWidgetManager.updateAppWidget(appWidgetId, widget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.quick_widget_list);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
