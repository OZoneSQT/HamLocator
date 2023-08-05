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
     * @param value as five digits string
     * @return
     */
    String fiveDigitsDoubleToString(double value);

    /**
     * double formatter
     * @param value as two digits string
     * @return
     */
    String twoDigitsDoubleToString(double value);

}
