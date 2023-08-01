package dk.seahawk.hamlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import dk.seahawk.hamlocator.algorithm.CoordinateConverter;
import dk.seahawk.hamlocator.algorithm.GridAlgorithm;
import dk.seahawk.hamlocator.algorithm.GridAlgorithmInterface;
import dk.seahawk.hamlocator.util.LocationHistory;

public class MainActivity extends AppCompatActivity {


    private CoordinateConverter coordinateConverter = new CoordinateConverter();
    private LocationHistory locationHistory;
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

    private int interval = 10000;
    private int fastInterval = 5000;
    private int priority = LocationRequest.QUALITY_HIGH_ACCURACY;

    private GridAlgorithmInterface gridAlgorithm;
    private TextView jidField, lonField, latField, altField, nsLonField, ewLatField;
    private String TAG = "Locator";

    /**
     * Solved issue:
     * https://stackoverflow.com/questions/29441384/fusedlocationapi-getlastlocation-always-null
     * https://stackoverflow.com/questions/61777386/fusedlocationclient-getlastlocation-always-returns-null
     */
    private FusedLocationProviderClient fusedLocationProviderClient;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize location client
        // My issue has been that i generated a new fusedLocationProviderClient, not calling it from LocationServices
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        gridAlgorithm = new GridAlgorithm();


        setContentView(R.layout.activity_main);

        /*
            // Initialize location client
            // My issue has been that i generated a new fusedLocationProviderClient, not calling it from LocationServices
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            gridAlgorithm = new GridAlgorithm();
            locationHistory = locationHistory.getInstance();

            // Initialize view
            binding = FragmentLocatorBinding.inflate(inflater, container, false);
            View root = binding.getRoot();
            jidField = root.findViewById(R.id.txt_jid);
            lonField = root.findViewById(R.id.txt_longitude);
            latField = root.findViewById(R.id.txt_latitude);
            altField = root.findViewById(R.id.txt_altitude);
            nsLonField = root.findViewById(R.id.txt_lon_dms_ns);
            ewLatField = root.findViewById(R.id.txt_lat_dms_ew);

            checkCondition();

            FloatingActionButton fab = root.findViewById(R.id.fab);
            fab.setOnClickListener(v -> setPlaceholderItem(lastKnownLocation));

         */
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check condition
        if (requestCode == priority && (grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            // When permission are granted
            Log.d(TAG, "Success: settingsCheck");
            getCurrentLocation();
        } else {
            // When permission are denied, Display toast
            Log.d(TAG, "Fail: settingsCheck, Permission denied");
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        // Initialize Location manager
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        // Check condition
        if (locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // When location service is enabled
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                // Initialize location
                Location location = task.getResult();
                // Check condition
                if (location != null) {
                    Log.d(TAG, "Success: latitude " + location.getLatitude() + " longitude " + location.getLongitude());
                    updatedLocation(location);
                } else {
                    Log.d(TAG, "location is null");
                    setLocationRequest();
                    setLocationCallback();
                }
            });
        } else {
            // When location service is not enabled, open location setting
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void setLocationRequest() {
        // When location result is null, initialize location request
        locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(priority);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastInterval);
        locationRequest.setNumUpdates(1);
        Log.d(TAG, "locationRequest is set");
    }

    @SuppressLint("MissingPermission")
    private void setLocationCallback() {
        // Initialize location call back
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void
            onLocationResult(LocationResult locationResult) {
                // Initialize location
                Location location = locationResult.getLastLocation();
                updatedLocation(location);
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void checkCondition() {
        // check condition
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // When permission is granted
            getCurrentLocation();
           // Log.d(TAG, "location permissions granted");
        } else {
            // When permission is not granted
            requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 100);
           // Log.d(TAG, "location permissions denied");
        }
    }

    private void updatedLocation(@NonNull Location location) {
        this.lastKnownLocation = location;
        jidField.setText(gridAlgorithm.getGridLocation(location));
        lonField.setText(coordinateConverter.fiveDigitsDoubleToString(location.getLongitude()));
        latField.setText(coordinateConverter.fiveDigitsDoubleToString(location.getLatitude()));
        altField.setText(coordinateConverter.twoDigitsDoubleToString(location.getAltitude()));
        nsLonField.setText(coordinateConverter.getLon(location.getLongitude()));
        ewLatField.setText(coordinateConverter.getLat(location.getLatitude()));

        Log.d(TAG, "location updated in layout");
    }

    public void onLocationChanged(@NonNull Location location) {
        updatedLocation(location);
        Log.d(TAG, "location changed");
    }

    private void setPlaceholderItem(Location location) {
        if(location != null) {
            String dateTimeFormat = "HH:mm:ssZZ dd/mm/yyyy";
            String utc = "Etc/UTC"; // https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
            String localTime, utcTime;

            // NOTE: minimum SDK 26 / Android 8.0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);  // API level 26
                ZonedDateTime currentTimeLocal = ZonedDateTime.now();                               // API level 26
                ZonedDateTime currentTimeUTC = currentTimeLocal.withZoneSameLocal(ZoneId.of(utc));  // API level 26

                localTime = "local:" + dateTimeFormatter.format(currentTimeLocal);
                utcTime = "  utc:" + dateTimeFormatter.format(currentTimeUTC);
            } else {
                TimeZone utcTimeZone = TimeZone.getTimeZone(utc);
                Calendar calendar = java.util.Calendar.getInstance();              // API level 1

                localTime = getTimeLocal(calendar);
                utcTime = getTimeUTC(calendar, utcTimeZone);
            }

            String jid = gridAlgorithm.getGridLocation(location);
            String lat = "lat: " + location.getLatitude();
            String lon = "lon: " + location.getLongitude();
            String alt = "alt: " + coordinateConverter.twoDigitsDoubleToString(location.getAltitude()) + " m";

            locationHistory.addItemToList((new LocationHistory.PlaceholderItem(jid, localTime, utcTime, lat, lon, alt)));
            Log.d(TAG, "PlaceholderItem sent to RecyclerView/History");
        } else {
            Log.d(TAG, "PlaceholderItem is not sent to RecyclerView/History, Location = null");
        }
    }

    /*
        The IDE suggest to use Calendar.get(Calendar.MINUTE) ect. but I am not interested
        in the current/updated time, i want to use the same instance in multiply methods
        It would be preferable to use DateTimeFormatter, but it is first available from API level 26
     */
    private String dateBuilder(Calendar calendar){
        Date date = calendar.getTime();
        return date.getMinutes() + ":" + date.getMinutes() + ":" + date.getMinutes() + " " + DateFormat.getDateInstance().format(date);
    }

    private String getTimeLocal(Calendar calendar) {
        return "local:" + dateBuilder(calendar);
    }

    private String getTimeUTC(Calendar calendar, TimeZone utcTimeZone) {
        calendar.setTimeZone(utcTimeZone);
        return "  utc:" + dateBuilder(calendar);
    }

}