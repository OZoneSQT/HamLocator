package dk.seahawk.hamlocator.algorithm;

import android.annotation.SuppressLint;

/**
 * Convert GPS coordinates from DD (Decimal degrees (google maps coordinates)) to DMS (Degrees Minutes Seconds (map coordinates))
 */
public class CoordinateConverter implements CoordinateConverterInterface {

    public CoordinateConverter() {}

    /*
        dd = Decimal Degrees
        d = Degrees
        m = Minutes
        s = Seconds
     */
    private String convert(double dd) {
        int d = (int)dd;
        double dm = ((dd - d) * 60);
        int m = (int)dm;
        double s = ((dm - m) * 60);

        return String.valueOf(d) + "\u00B0" + String.valueOf(m) + "\'" + twoDigitsDoubleToString(s) + "\"";
    }

    // Negative latitudes range from 0 to -90° and are found south of the equator.
    public String getLat(double lat) {
        String result = "S";
        if (lat > 0) result = "N";
        return result + convert(lat);
    }

    // Negative longitudes range from 0 to -180° and are found west of the prime meridian.
    public String getLon(double lon) {
        String result = "W";
        if (lon > 0) result = "E";
        return result + convert(lon);
    }

    // double formatter
    @SuppressLint("DefaultLocale")
    public String fiveDigitsDoubleToString(double value) {
        return String.format("%.5f", value);
    }

    // double formatter
    @SuppressLint("DefaultLocale")
    public String twoDigitsDoubleToString(double value) {
        return String.format("%.2f", value);
    }

}
