package dk.seahawk.hamlocator.util;

import android.content.Context;
import android.util.Log;

import java.util.Locale;

public class Unit {

    private final String TAG;
    private final Context context;

    public Unit(String TAG, Context context) {
        this.TAG = TAG;
        this.context = context;
    }

    public boolean isMetric() {
        Locale locale = context.getResources().getConfiguration().locale;

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

    public String getUnit() {
        String unit = " m";
        if(!isMetric()) unit = " ft";
        return unit;
    }

    public boolean isUsingFahrenheit() {
        Locale locale = Locale.getDefault();
        String countryCode = locale.getCountry();

        // List of countries where Fahrenheit is commonly used
        String[] fahrenheitCountries = {"US", "BS", "BZ", "KY"};

        for (String fahrenheitCountry : fahrenheitCountries) {
            if (countryCode.equalsIgnoreCase(fahrenheitCountry)) {
                return true;
            }
        }

        return false;
    }

}