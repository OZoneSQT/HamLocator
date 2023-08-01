package dk.seahawk.hamlocator.algorithm.gridcalc;

import dk.seahawk.hamlocator.util.MyExceptionHandler;

public class Square {

    public Square() {
    }

    public String getSquare(int longitudeSquare, int latitudeSquare) {
        return getLocationSquare(longitudeSquare) + getLocationSquare(latitudeSquare);
    }

    private String getLocationSquare(int square) {
        String result;

        try {
            if (0 == square) {
                result = "0";
            } else if (1 == square) {
                result = "1";
            } else if (2 == square) {
                result = "2";
            } else if (3 == square) {
                result = "3";
            } else if (4 == square) {
                result = "4";
            } else if (5 == square) {
                result = "5";
            } else if (6 == square) {
                result = "6";
            } else if (7 == square) {
                result = "7";
            } else if (8 == square) {
                result = "8";
            } else if (9 == square) {
                result = "9";
            } else {
                throw new IllegalStateException();
            }
        } catch (IllegalStateException e) {
            MyExceptionHandler exceptionHandler = new MyExceptionHandler();
            exceptionHandler.pushExceptionMessage("Error in calculating square grid");
            result = "Ø";
        }

        return result;
    }

}
