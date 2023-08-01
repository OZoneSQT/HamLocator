package dk.seahawk.hamlocator.algorithm.gridcalc;

public class MaidenheadLatitude {
    private double latitudeFieldRemainder;
    private double latitudeSquareRemainder;

    public MaidenheadLatitude() {
    }

    public int calcLatitudeField(double latitude) {
        double field = latitude + 90;
        this.latitudeFieldRemainder = field % 10;
        return (int) field / 10;
    }

    public int calcLatitudeSquare() {
        this.latitudeSquareRemainder = latitudeFieldRemainder % 1;
        return (int) latitudeFieldRemainder / 1;
    }

    public int calcLatitudeSubSquare() {
        double subSquare = latitudeSquareRemainder / 0.0416;
        return (int) subSquare;
    }

}
