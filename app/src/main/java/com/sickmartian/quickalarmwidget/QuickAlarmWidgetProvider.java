package com.sickmartian.quickalarmwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by ***REMOVED*** on 8/9/16.
 */
public class QuickAlarmWidgetProvider extends AppWidgetProvider {
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d("QAWP", "onAppWidgetOptionsChanged");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("QAWP", "onUpdate");
        for (int i=0; i<appWidgetIds.length; i++) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            Intent svcIntent = new Intent(context, QuickAlarmWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.quick_widget_layout);

            widget.setRemoteAdapter(appWidgetIds[i], R.id.quick_widget_list, svcIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
