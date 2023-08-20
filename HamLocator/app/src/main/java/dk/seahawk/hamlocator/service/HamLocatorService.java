
package dk.seahawk.hamlocator.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;

public class HamLocatorService extends IntentService {

    private ResultReceiver mReceiver;
  //  private Activity activity;
    private String TAG = "HamLocatorService";

    public static final String RECEIVER_KEY = "receiver";



    public HamLocatorService() {
        super("HamLocatorService");
        Log.d(TAG, "init HamLocatorService");
   //     this.activity = new Activity();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        // Get the result receiver from the intent
        mReceiver = intent.getParcelableExtra("receiver");

        // Get the current location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            Log.d(TAG, "Request permissions");
         //   ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
          //  Toast.makeText(activity, "Location permission granted", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = LocationServices.getFusedLocationProviderClient(this).getLastLocation().getResult();
        Log.d(TAG, "Updated location = lon:" + location.getLongitude() + " , lat: " + location.getLatitude());

        // Send the location to the result receiver
        Bundle resultData = new Bundle();
        resultData.putParcelable("location", location);
        mReceiver.send(Activity.RESULT_OK, resultData);
        Log.d(TAG, "Location updated");
    }
}