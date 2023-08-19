package dk.seahawk.hamlocator.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.widget.RemoteViews;
import android.os.ResultReceiver;

import dk.seahawk.hamlocator.R;
import dk.seahawk.hamlocator.algorithm.GridAlgorithm;
import dk.seahawk.hamlocator.algorithm.GridAlgorithmInterface;
import dk.seahawk.hamlocator.service.HamLocatorService;

/**
 * Implementation of App Widget functionality.
 */
public class HamLocatorWidget extends AppWidgetProvider {

    private ResultReceiver mReceiver;
    private String TAG = "HamLocatorWidget";
    private Context context;
    private AppWidgetManager appWidgetManager;
    private int appWidgetId;

    private String ACTION_LOCATION_UPDATE = "dk.seahawk.hamlocator.widget.hamLocatorWidget.ACTION_LOCATION_UPDATE";
    private String EXTRA_LOCATION = "dk.seahawk.hamlocator.widget.hamLocatorWidget.EXTRA_LOCATION";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        //TODO GET LOCATION FROM SERVICE
        //Location location =
        //GridAlgorithmInterface gridAlgorithmInterface = new GridAlgorithm();
        //String grid = gridAlgorithmInterface.getGridLocation(location);

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ham_locator_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.appWidgetManager = appWidgetManager;
        this.context = context;

        // Create a ResultReceiver object
        mReceiver = new ResultReceiver(new Handler());

        // Start the IntentService
        Intent intent = new Intent(context, HamLocatorService.class);
        intent.putExtra("receiver", mReceiver);
        context.startService(intent);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            this.appWidgetId = appWidgetId;
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_LOCATION_UPDATE)) {
            // Get the location from the intent
            Location location = intent.getParcelableExtra(EXTRA_LOCATION);

            // Update the text view with the location
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ham_locator_widget);
            views.setTextViewText(R.id.textView, location.toString());
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}