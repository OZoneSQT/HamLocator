package dk.seahawk.hamlocator.widget;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;

import dk.seahawk.hamlocator.R;
import dk.seahawk.hamlocator.algorithm.GridAlgorithm;
import dk.seahawk.hamlocator.algorithm.GridAlgorithmInterface;
import dk.seahawk.hamlocator.util.ActivityHolder;


/**
 * Implementation of App Widget functionality.
 */
public class HamLocatorWidget extends AppWidgetProvider {

    /*
     * LocationRequest
     *
     * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
     * setFastestInterval(long millis) - Explicitly set the fastest interval for location updates, in milliseconds.
     * setInterval(long millis) - Set the desired interval for active location updates, in milliseconds.
     * setPriority(int) Options:
     *                          PRIORITY_HIGH_ACCURACY (100) - Used to request the most accurate locations available.
     *                          PRIORITY_BALANCED_POWER_ACCURACY (102) - Used to request "block" level accuracy.
     *                          PRIORITY_LOW_POWER (104) - Used to request "city" level accuracy.
     *                          PRIORITY_NO_POWER (105) - Used to request the best accuracy possible with zero additional power consumption.
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    // Counter
    private static final int UPDATE_INTERVAL = 1000; // 1 second
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    // private int counter = 0;
    // private int MAX_COUNTER = 99999;
    private String TAG = "HamLocatorWidget";


    // Util
    private Activity activity;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Create the initial updateRunnable
        Log.d(TAG, "onUpdate() - Create the initial updateRunnable");
        if (activity == null) initWidget();

        updateRunnable = new Runnable() {
            @Override
            public void run() {

            /*
                // Update the counter and reset if it reaches the maximum value
                Log.d(TAG, "run() - Update the counter and reset if it reaches the maximum value");
                counter = (counter + 1) % (MAX_COUNTER + 1);

                // Update the widget UI
                Log.d(TAG, "run() - Update the widget UI");
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ham_locator_widget);
                remoteViews.setTextViewText(R.id.appwidget_text, String.valueOf(counter));
                appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
             */

                // Update the widget UI
                Log.d(TAG, "run() - Update the widget UI");
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ham_locator_widget);
                remoteViews.setTextViewText(R.id.appwidget_text, locationHandler());
                remoteViews.setTextViewText(R.id.appwidget_header, context.getString(R.string.app_name));

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
        Log.d(TAG, "onEnabled() - Widget is enabled");
        initWidget();
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled() - Widget is removed");
        handler.removeCallbacks(updateRunnable);
    }


    /**
     *  Util
     */
    private void initWidget() {
        Log.d(TAG, "initWidget()");
        ActivityHolder activityHolder = ActivityHolder.getInstance();
        activity = activityHolder.getActivity();
    }


    /**
     *  Location
     */
    private String locationHandler() {
        Log.d(TAG, "locationHandler()");
        if (activity == null) initWidget();

        try {
            if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission not granted ");
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                Log.d(TAG, "Permission granted ");
            }
        } catch (Exception e) {
            Log.e(TAG, "locationHandler() -> Permission check\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        GridAlgorithmInterface gridAlgorithmInterface = new GridAlgorithm();
        Location location = fusedLocationProviderClient.getLastLocation().getResult();

        Log.d(TAG, "location == null " + (location == null));
        assert location != null;

        return gridAlgorithmInterface.getGridLocation(location);
    }

}