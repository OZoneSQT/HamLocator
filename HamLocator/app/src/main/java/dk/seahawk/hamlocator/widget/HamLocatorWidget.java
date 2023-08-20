package dk.seahawk.hamlocator.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.os.Handler;

import dk.seahawk.hamlocator.R;


/**
 * Implementation of App Widget functionality.
 */
public class HamLocatorWidget extends AppWidgetProvider {

    private static final int UPDATE_INTERVAL = 100; // 1 second
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    private int counter = 0;
    private int MAX_COUNTER = 99999;
    private String TAG = "HamLocatorWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Create the initial updateRunnable
        Log.d(TAG, "onUpdate() - Create the initial updateRunnable");

        updateRunnable = new Runnable() {
            @Override
            public void run() {

                // Update the counter and reset if it reaches the maximum value
                Log.d(TAG, "run() - Update the counter and reset if it reaches the maximum value");
                counter = (counter + 1) % (MAX_COUNTER + 1);

                // Update the widget UI
                Log.d(TAG, "run() - Update the widget UI");
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ham_locator_widget);
                remoteViews.setTextViewText(R.id.appwidget_text, String.valueOf(counter));
                appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

                // Schedule the next update
                Log.d(TAG, "run() - Schedule the next update");
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };

        // Start the initial update
        Log.d(TAG, "onUpdate() - Start the initial update");
        handler.post(updateRunnable);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Remove the update runnable when the last widget is removed
        Log.d(TAG, "onDisabled() - Remove the update runnable when the last widget is removed");
        handler.removeCallbacks(updateRunnable);
    }

}