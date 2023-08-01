package dk.seahawk.hamlocator.algorithm;

import android.location.Location;

import dk.seahawk.mockup.LocationCard;

/**
 * Sub square size = 3 x 4 miles or, 4,82803 x 6.43738 kilometres
 */
public interface GridAlgorithmInterface {

    /**
     * Get grid location in maidenhead-grid-square-locator-format
     * @param location
     * @return
     */
    String getGridLocation(Location location);

    /**
     * Get grid location in maidenhead-grid-square-locator-format
     * @param longitude
     * @param latitude
     * @return
     */
    String getGridLocation(double longitude, double latitude);

    /**
     * Get altitude, from gps
     * @return
     */
    String getAltitude();

    /**
     * Test method
     * @param muckUpLocation
     * @return
     */
    String getGridLocationTestMethod(LocationCard muckUpLocation);
}
