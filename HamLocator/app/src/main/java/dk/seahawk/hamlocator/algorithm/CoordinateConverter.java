package dk.seahawk.hamlocator.algorithm;


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

        return d + "\u00B0" + m + "\'" + digitsDoubleToString(2, s) + "\"";
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

    @Override
    public String digitsDoubleToString(int digits, double value) {
        return String.format("%."+ digits +"f", value);
    }

}
