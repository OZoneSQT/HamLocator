package dk.seahawk.hamlocator.algorithm;

public interface CoordinateConverterInterface {

    /**
     * Convert latitude to a string
     * Negative latitudes range from 0 to -90° and are found south of the equator.
     * @param lat as string
     * @return
     */
    String getLat(double lat);

    /**
     * Convert longitude to a string
     * Negative longitudes range from 0 to -180° and are found west of the prime meridian.
     * @param lon as string
     * @return
     */
    String getLon(double lon);

    /**
     * double formatter
     * @param digits number of digits
     * @param value value to convert
     * @return
     */
    String digitsDoubleToString(int digits, double value);
}
