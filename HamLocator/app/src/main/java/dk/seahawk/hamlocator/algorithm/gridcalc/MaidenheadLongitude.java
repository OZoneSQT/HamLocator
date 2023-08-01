package dk.seahawk.hamlocator.algorithm.gridcalc;

public class MaidenheadLongitude {
    private double longitudeFieldRemainder;
    private double longitudeSquareRemainder;

    public MaidenheadLongitude() {
    }

    public int calcLongitudeField(double longitude) {
        double field = longitude + 180;
        this.longitudeFieldRemainder = field % 20;
        int result = (int) field / 20;
        return result;
    }

    public int calcLongitudeSquare() {
        this.longitudeSquareRemainder = longitudeFieldRemainder % 2;
        int result = (int) longitudeFieldRemainder / 2;
        return result;
    }

    public int calcLongitudeSubSquare() {
        double subSquare = longitudeSquareRemainder / 0.083333;
        return (int) subSquare;
    }

}
