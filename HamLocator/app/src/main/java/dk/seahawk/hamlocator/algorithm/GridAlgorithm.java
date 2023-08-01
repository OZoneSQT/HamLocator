package dk.seahawk.hamlocator.algorithm;

import android.location.Location;

import dk.seahawk.hamlocator.algorithm.gridcalc.Field;
import dk.seahawk.hamlocator.algorithm.gridcalc.MaidenheadLatitude;
import dk.seahawk.hamlocator.algorithm.gridcalc.MaidenheadLongitude;
import dk.seahawk.hamlocator.algorithm.gridcalc.Square;
import dk.seahawk.hamlocator.algorithm.gridcalc.SubSquare;
import dk.seahawk.mockup.LocationCard;


// https://en.wikipedia.org/wiki/Maidenhead_Locator_System
public class GridAlgorithm implements GridAlgorithmInterface {
    private double longitude, latitude, altitude;
    private int longitudeField, latitudeField, longitudeSquare, latitudeSquare, longitudeSubSquare, latitudeSubSquare;
    private Field field;
    private Square square;
    private SubSquare subSquare;
    private MaidenheadLongitude maidenheadLongitude;
    private MaidenheadLatitude maidenheadLatitude;

    public GridAlgorithm() {
        field = new Field();
        square = new Square();
        subSquare = new SubSquare();
        maidenheadLongitude = new MaidenheadLongitude();
        maidenheadLatitude = new MaidenheadLatitude();
    }

    public String getGridLocation(Location location) {
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.altitude = location.getAltitude();
        parseLocation();

        return getGridSquares();
    }

    @Override
    public String getGridLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        parseLocation();

        return getGridSquares();
    }

    // https://www.dxzone.com/grid-square-locator-system-explained/
    private void parseLocation() {
        // Field
        this.longitudeField = maidenheadLongitude.calcLongitudeField(longitude);
        this.latitudeField = maidenheadLatitude.calcLatitudeField(latitude);
        // Square
        this.longitudeSquare = maidenheadLongitude.calcLongitudeSquare();
        this.latitudeSquare = maidenheadLatitude.calcLatitudeSquare();
        // SubSquare
        this.longitudeSubSquare = maidenheadLongitude.calcLongitudeSubSquare();
        this.latitudeSubSquare = maidenheadLatitude.calcLatitudeSubSquare();
    }

    private String getGridSquares() {
        return field.getField(longitudeField, latitudeField) +
                square.getSquare(longitudeSquare, latitudeSquare) +
                subSquare.getSubSquare(longitudeSubSquare, latitudeSubSquare);
    }

    public String getAltitude() {
        return String.valueOf(altitude);
    }

    // Test method for, muck up test of location
    @Override
    public String getGridLocationTestMethod(LocationCard muckUpLocation) {
        this.longitude = muckUpLocation.getLongitudeNum();
        this.latitude = muckUpLocation.getLatitudeNum();
        this.altitude = muckUpLocation.getAltitudeNum();
        parseLocation();

        System.out.print(" ( ( ( longitude + 180 ) % 20 )  & ");
        System.out.print(" (int) ( longitude + 180 ) / 20 -> ");
        System.out.println(" Value of longitudeField: " + longitudeField);

        System.out.print(" ( ( latitude + 90 ) % 10 )  & ");
        System.out.print(" (int) ( latitude + 90 ) / 10  -> ");
        System.out.println(" Value of latitudeField: " + latitudeField);

        System.out.print(" ( ( longitudeFieldRemainder % 2 )  & ");
        System.out.print(" ( (int) ( longitudeFieldRemainder / 2 )  -> ");
        System.out.println(" Value of longitudeSquare: " + longitudeSquare);

        System.out.print(" ( ( latitudeFieldRemainder % 1 )  & ");
        System.out.print(" ( (int) ( latitudeFieldRemainder / 1 )  -> ");
        System.out.println(" Value of latitudeSquare: " + latitudeSquare);

        System.out.print(" (int) ( longitudeSquareRemainder / 0.083333  -> ");
        System.out.println(" Value of longitudeSubSquare: " + longitudeSubSquare);

        System.out.print(" (int) ( latitudeSquareRemainder / 0.0416  -> ");
        System.out.println(" Value of latitudeSubSquare: " + latitudeSubSquare);

        String result = getGridSquares();
        System.out.print(" Longitude: " + longitude + ", Latitude: " + latitude + ",");
        System.out.println(" Location grid id = " + result);

        return result;
    }

}
