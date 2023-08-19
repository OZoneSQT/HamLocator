package dk.seahawk.hamlocator.app;

import static com.google.android.gms.location.LocationRequest.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import dk.seahawk.hamlocator.R;
import dk.seahawk.hamlocator.algorithm.CoordinateConverter;
import dk.seahawk.hamlocator.algorithm.CoordinateConverterInterface;
import dk.seahawk.hamlocator.algorithm.GridAlgorithm;
import dk.seahawk.hamlocator.algorithm.GridAlgorithmInterface;
import dk.seahawk.hamlocator.util.Unit;

/*
 *  Log.x(Tag, msg) / Log.x(Tag, msg, tw)
 *  a = Assert
 *  d = Debug
 *  e = Error
 *  i = Info
 *  v = Verbose
 *  w = Warn
 */
public class MainActivity extends AppCompatActivity {

    // Location request
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

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
    private int INTERVAL = 1000;   // location refresh rate

    // Location handling
    private GridAlgorithmInterface gridAlgorithmInterface;
    private CoordinateConverterInterface coordinateConverterInterface;
    private TextView jidField, lonField, latField, altField, accField, nsLonField, ewLatField, localTimeField, utcTimeField;
    private final String TAG = "MainLocatorActivity";
    private String lastLocation = "na";
    private double lastLongitude = 0, lastLatitude = 0, lastAltitude = 0, lastAccuracy = 0;
    private Unit unit;

    // Time
    private Handler handler;
    private Runnable updateTimeRunnable;

    // Sign in
    private static final int RC_SIGN_IN = 1000;


    /**
     *  Life cycles
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initStaticUI();
        timeHandler();
        initLocationUI();
        locationHandler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
        handler.post(updateTimeRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        handler.post(updateTimeRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        handler.removeCallbacks(updateTimeRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
        handler.removeCallbacks(updateTimeRunnable);
    }


    /**
     * UI
     */
    private void initStaticUI() {
        Log.d(TAG, "init static UI");

        // Initialize view, Link
        TextView linkField = findViewById(R.id.txt_link);

        linkField.setText(Html.fromHtml("<a href='https://seahawk.dk'>Seahawk.dk</a>"));
        linkField.setMovementMethod(LinkMovementMethod.getInstance());

        FloatingActionButton fabSendEmail = findViewById(R.id.fabSendEmail);
        fabSendEmail.setOnClickListener(view -> {
            Log.d(TAG, "Floating action button pressed");
            sendEmail();
        });
    }

    private void initLocationUI() {
        Log.d(TAG, "init location UI");

        // Initialize view, Location
        jidField = findViewById(R.id.txt_jid);
        lonField = findViewById(R.id.txt_longitude);
        latField = findViewById(R.id.txt_latitude);
        altField = findViewById(R.id.txt_altitude);
        accField = findViewById(R.id.txt_accuracy);
        nsLonField = findViewById(R.id.txt_lon_dms_ns);
        ewLatField = findViewById(R.id.txt_lat_dms_ew);
    }


    /**
     *  Location
     */
    private void locationHandler() {
        Log.d(TAG, "init location handler");

        // Initialize Maidenhead algorithm
        gridAlgorithmInterface = new GridAlgorithm();
        coordinateConverterInterface = new CoordinateConverter();
        unit = new Unit(TAG, this);

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the LocationCallback
        locationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                // Update location parameters
                lastLocation = gridAlgorithmInterface.getGridLocation(location);
                assert location != null;
                lastLatitude = location.getLatitude();
                lastLongitude = location.getLongitude();
                lastAltitude = location.getAltitude();
                lastAccuracy = location.getAccuracy();

                // Update location text views
                jidField.setText(lastLocation);
                lonField.setText(getString(R.string.lon) + coordinateConverterInterface.digitsDoubleToString(7, lastLongitude));
                latField.setText(getString(R.string.lat) + coordinateConverterInterface.digitsDoubleToString(7, lastLatitude));
                nsLonField.setText(getString(R.string.lon) + coordinateConverterInterface.getLon(lastLongitude));
                ewLatField.setText(getString(R.string.lat) + coordinateConverterInterface.getLat(lastLatitude));

                // Update altitude text view
                altField.setText(getString(R.string.alt) + coordinateConverterInterface.digitsDoubleToString(2, lastAltitude) + unit.getUnit());
                accField.setText("Accuracy: " + coordinateConverterInterface.digitsDoubleToString(2, lastAccuracy) + unit.getUnit());

                Log.d(TAG, "Location updated: " + jidField);
            }
        };

        permissionCheck();
        locationRequest.setInterval(INTERVAL);
    }

    private void permissionCheck() {
        Log.d(TAG, "Permission check");

        // Check for location permission and request if not granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Request location updates
            startLocationUpdates();
        }
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
        Log.d(TAG, "Start location updates");

        locationRequest = create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);

        // Start location updates with the FusedLocationProviderClient
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "Stop location updates");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


    /**
     * Time
     */
    private void timeHandler() {
        Log.d(TAG, "init time handler");

        // Initialize view, Location
        localTimeField = findViewById(R.id.txt_localTime);
        utcTimeField = findViewById(R.id.txt_utcTime);

        handler = new Handler();
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimes();
                handler.postDelayed(this, 1000); // Update every 1 second
            }
        };
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    private void updateTimes() {
        Log.d(TAG, "Update time");

        String format = "dd-MM-yyyy HH:mm:ss";

        // Get date and set format
        Date date = new Date();
        if (!unit.isMetric()) format = "yyyy-MM-dd HH:mm:ss";

        // Get the current time in UTC
        SimpleDateFormat utcFormat = new SimpleDateFormat(format);
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Get the current time in local timezone
        SimpleDateFormat localFormat = new SimpleDateFormat(format);

        // Update the TextViews with the times
        utcTimeField.setText("UTC: " + utcFormat.format(date));
        localTimeField.setText("Local: " + localFormat.format(date));

        Log.d(TAG, "Time updated");
    }


    /**
     * Sign In
     */
    // https://developer.android.com/training/sign-in
    // https://github.com/easy-tuto/MyLoginApp/tree/login_with_google (example)
    private void signIn() {
        Log.d(TAG, "Sign in");

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);
            } catch (ApiException e) {
                Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }


    /**
     * Backup / Email intent
     */
    @SuppressLint("IntentReset")
    private void sendEmail() {
        try {
            String userEmail = "";

            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (googleSignInAccount != null) {
                userEmail = googleSignInAccount.getEmail();
            } else {
                signIn();
            }

            // Add content to message
            Log.d(TAG,  "Preparing mail");
            String[] TO = { userEmail };
            String subject = "HamLocator: " + jidField.getText() + ", utc:" + utcTimeField.getText();
            String body =    "Saved location from Android app \"HamLocator\":" +
                       "\n    JID-grid:   " + jidField.getText() +
                       "\n    DD:         " + lonField.getText() + " " + latField.getText() +
                       "\n    DMS:        " + nsLonField.getText() + " " + ewLatField.getText() +
                       "\n    Altitude    " + altField.getText() + unit.getUnit() +
                       "\n    Accuracy    " + accField.getText() + unit.getUnit() +
                       "\n    Local time: " + localTimeField.getText() +
                       "\n    UTC time:   " + utcTimeField.getText();

            // Build message
            Log.d(TAG, "Building mail");
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            // Send email / message
            Log.d(TAG, "Sending mail");
            startActivity(emailIntent);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"ERROR: Location have NOT been send", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error in \"sendEmail()\": \n" + e.getMessage());
        }
    }

}