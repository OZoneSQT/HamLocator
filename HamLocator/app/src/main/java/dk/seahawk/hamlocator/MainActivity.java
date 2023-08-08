package dk.seahawk.hamlocator;

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
import android.view.View;
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
import java.util.Locale;
import java.util.TimeZone;

import dk.seahawk.hamlocator.algorithm.CoordinateConverter;
import dk.seahawk.hamlocator.algorithm.CoordinateConverterInterface;
import dk.seahawk.hamlocator.algorithm.GridAlgorithm;
import dk.seahawk.hamlocator.algorithm.GridAlgorithmInterface;

public class MainActivity extends AppCompatActivity {

    // Location request
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

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
    private int INTERVAL = 10000;   // location refresh rate

    // Location handling
    private GridAlgorithmInterface gridAlgorithmInterface;
    private CoordinateConverterInterface coordinateConverterInterface;
    private TextView jidField, lonField, latField, altField, nsLonField, ewLatField, localTimeField, utcTimeField, linkField;
    private String TAG = "MainLocatorActivity", lastLocation = "na";
    private double lastLongitude = 0, lastLatitude = 0, lastAltitude = 0;

    // Time
    private Handler handler;
    private Runnable updateTimeRunnable;

    // Sign in
    private String userEmail = "";
    private static final int RC_SIGN_IN = 9001;

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
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                // Update location parameters
                lastLocation = gridAlgorithmInterface.getGridLocation(location);
                assert location != null;
                lastLatitude = location.getLatitude();
                lastLongitude = location.getLongitude();
                lastAltitude = location.getAltitude();

                // Update location text views
                jidField.setText(lastLocation);
                lonField.setText(getString(R.string.lon) + coordinateConverterInterface.digitsDoubleToString(7, lastLongitude));
                latField.setText(getString(R.string.lat) + coordinateConverterInterface.digitsDoubleToString(7, lastLatitude));
                nsLonField.setText(getString(R.string.lon) + coordinateConverterInterface.getLon(lastLongitude));
                ewLatField.setText(getString(R.string.lat) + coordinateConverterInterface.getLat(lastLatitude));

                // Update altitude text view
                if(isMetric()) {
                    altField.setText(getString(R.string.alt) + coordinateConverterInterface.digitsDoubleToString(2, lastAltitude) + " m");
                } else {
                    altField.setText(getString(R.string.alt) + coordinateConverterInterface.digitsDoubleToString(2, lastAltitude) + " ft");
                }
                Log.d(TAG, "Location updated: " + jidField);
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

        FloatingActionButton fabSendEmail = findViewById(R.id.fabSendEmail);
        fabSendEmail.setOnClickListener(view -> {
            Log.d(TAG, "Floating action button pressed");
            sendEmail();
        });

        locationRequest.setInterval(INTERVAL);
    }

    boolean isMetric() {
        Locale locale = this.getResources().getConfiguration().locale;

        switch (locale.getCountry().toUpperCase()) {
            case "US": // Imperial (US)
            case "GB": // Imperial (United Kingdom)
            case "MM": // Imperial (Myanmar)
            case "LR": // Imperial (Liberia)
                Log.d(TAG, "Imperial Measurement unit");
                return false;
            default:
                Log.d(TAG, "Metric Measurement unit");
                return true;
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
        locationRequest = create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        // Start location updates with the FusedLocationProviderClient
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    private void updateTimes() {
        String format = "dd-MM-yyyy HH:mm:ss";

        // Get date and set format
        Date date = new Date();
        if (!isMetric()) format = "yyyy-MM-dd HH:mm:ss";

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

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(updateTimeRunnable);
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

    // https://developer.android.com/training/sign-in
    // https://developers.google.com/identity/sign-in/android/start-integrating
    // https://github.com/sunjithc/GoogleSignInAccount-getIdToken (example)
    private void signIn() {
        Log.d(TAG, "Sign in");

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

        Log.d(TAG, "Sign in check at log in");

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        userEmail = account.getEmail();

        Log.d(TAG, "USER MAIL: " + userEmail);
    }

    private void signInCheck() {
        Log.d(TAG, "Sign in check");
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount == null) signIn();
        if (userEmail == null) signIn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            Log.d("getIdToken", account.getIdToken());

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    //TODO Intent stops the application, debug:
    private void sendEmail() {
        signInCheck();

        Log.d(TAG, "Preparing mail");
        if (jidField != null || userEmail == "" ) {
            // Add content to message
            String[] TO = {userEmail};
            String[] CC = {};
            String subject = "HamLocator: " + jidField + ", utc:" + utcTimeField;
            String body = "Saved location from Android app \"HamLocator\" -> " + "\n" +
                    "JID-grid: " + jidField + "\n" +
                    "DD: " + lonField + " " + latField + "\n" +
                    "DMS: " + nsLonField + " " + ewLatField + "\n" +
                    "Altitude " + altField + "\n" +
                    "Local time: " + localTimeField + "\n" +
                    "UTC time: " + utcTimeField;

            Log.d(TAG, "Building mail");
            // Build message
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_CC, CC);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            startActivity(emailIntent);

            Log.d(TAG, "Sending mail");
            // Send email / message
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
               startActivity(emailIntent);
            }

            Toast.makeText(getApplicationContext(),"Location send to " + userEmail ,Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"ERROR: Location have NOT been send",Toast.LENGTH_LONG).show();
       }

    }

}