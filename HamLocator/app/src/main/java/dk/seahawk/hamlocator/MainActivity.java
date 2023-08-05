package dk.seahawk.hamlocator;

import static com.google.android.gms.location.LocationRequest.*;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import dk.seahawk.hamlocator.algorithm.CoordinateConverter;
import dk.seahawk.hamlocator.algorithm.CoordinateConverterInterface;
import dk.seahawk.hamlocator.algorithm.GridAlgorithm;
import dk.seahawk.hamlocator.algorithm.GridAlgorithmInterface;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private GridAlgorithmInterface gridAlgorithmInterface;
    private CoordinateConverterInterface coordinateConverterInterface;

    /**
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
    private int interval = 10000;
    private int fastInterval = 5000;


    private TextView jidField, lonField, latField, altField, nsLonField, ewLatField, localTimeField, utcTimeField, linkField;
    private String TAG = "MainLocatorActivity";
    private String lastLocation = "na";
    private double lastLongitude = 0;
    private double lastLatitude = 0;
    private double lastAltitude = 0;

    private Handler handler;
    private Runnable updateTimeRunnable;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize view, Location
        jidField = findViewById(R.id.txt_jid);
        lonField = findViewById(R.id.txt_longitude);
        latField = findViewById(R.id.txt_latitude);
        altField = findViewById(R.id.txt_altitude);
        nsLonField = findViewById(R.id.txt_lon_dms_ns);
        ewLatField = findViewById(R.id.txt_lat_dms_ew);

        // Initialize view, Location
        localTimeField = findViewById(R.id.txt_localTime);
        utcTimeField = findViewById(R.id.txt_utcTime);

        // Initialize view, Link
        linkField = findViewById(R.id.txt_link);

        // Initialize Maidenhead algorithm
        gridAlgorithmInterface = new GridAlgorithm();
        coordinateConverterInterface = new CoordinateConverter();

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the LocationCallback
        locationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();

                    // Update location parameters
                    lastLocation = gridAlgorithmInterface.getGridLocation(location);
                    lastLatitude = location.getLatitude();
                    lastLongitude = location.getLongitude();
                    lastAltitude = location.getAltitude();

                    jidField.setText(lastLocation);
                    lonField.setText("lon: " + lastLongitude);
                    latField.setText("lat: " + lastLatitude);
                    altField.setText("alt: " + coordinateConverterInterface.twoDigitsDoubleToString(lastAltitude) + " m");
                    nsLonField.setText("lon dms: " + coordinateConverterInterface.getLon(lastLongitude));
                    ewLatField.setText("lat dms: " + coordinateConverterInterface.getLat(lastLatitude));
                }
            }
        };

        // Check for location permission and request if not granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Request location updates
            startLocationUpdates();
        }

        handler = new Handler();
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimes();
                handler.postDelayed(this, 1000); // Update every 1 second
            }
        };

        linkField.setText(Html.fromHtml("<a href='https://seahawk.dk'>Seahawk.dk</a>"));
        linkField.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // When permission are denied, Display toast
                Log.d(TAG, "Fail: settingsCheck, Permission denied");
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Update interval in milliseconds (e.g., 5000ms = 5 seconds)

        // Start location updates with the FusedLocationProviderClient
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    private void updateTimes() {
        // Get the current time in UTC
        Date utcDate = new Date();
        SimpleDateFormat utcFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = utcFormat.format(utcDate);

        // Get the current time in local timezone
        Date localDate = new Date();
        SimpleDateFormat localFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String localTime = localFormat.format(localDate);

        // Update the TextViews with the times
        utcTimeField.setText("UTC: " + utcTime);
        localTimeField.setText("Local: " + localTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateTimeRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove location updates when the activity is paused or stopped
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        handler.removeCallbacks(updateTimeRunnable);
    }

}