package com.amargodigits.uka_chan.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import com.amargodigits.uka_chan.MainActivity;
import com.amargodigits.uka_chan.R;
import static android.content.Context.MODE_PRIVATE;
import static com.amargodigits.uka_chan.MainActivity.LOG_TAG;

/**
 * Implementation of App Widget functionality.
 */
public class UkaWidgetProvider extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.uka_widget_provider);
        Intent intent = new Intent(context, MainActivity.class);
        Log.i(LOG_TAG, "UkaWidgetProvider updateAppWidget ");
        intent.setAction("uka.widget");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_main_layout, pendingIntent);
        final String PREFS_NAME = "UkaWidget";
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String songName = prefs.getString("songName", context.getString(R.string.app_name));
        String songText = prefs.getString("songText", context.getString(R.string.select_song));
        views.setTextViewText(R.id.appwidget_title, songName);
        views.setTextViewText(R.id.appwidget_text, songText);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

