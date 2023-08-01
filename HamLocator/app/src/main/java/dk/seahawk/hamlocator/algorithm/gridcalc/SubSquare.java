package dk.seahawk.hamlocator.algorithm.gridcalc;

import dk.seahawk.hamlocator.util.MyExceptionHandler;

public class SubSquare {

    public SubSquare() {
    }

    public String getSubSquare(int longitudeSubSquare, int latitudeSubSquare) {
        return getLocationSubSquare(longitudeSubSquare) + getLocationSubSquare(latitudeSubSquare);
    }

    private String getLocationSubSquare(int subSquare) {
        String result;

        try {
            if (0 == subSquare) {
                result = "a";
            } else if (1 == subSquare) {
                result = "b";
            } else if (2 == subSquare) {
                result = "c";
            } else if (3 == subSquare) {
                result = "d";
            } else if (4 == subSquare) {
                result = "e";
            } else if (5 == subSquare) {
                result = "f";
            } else if (6 == subSquare) {
                result = "g";
            } else if (7 == subSquare) {
                result = "h";
            } else if (8 == subSquare) {
                result = "i";
            } else if (9 == subSquare) {
                result = "j";
            } else if (10 == subSquare) {
                result = "k";
            } else if (11 == subSquare) {
                result = "l";
            } else if (12 == subSquare) {
                result = "m";
            } else if (13 == subSquare) {
                result = "n";
            } else if (14 == subSquare) {
                result = "o";
            } else if (15 == subSquare) {
                result = "p";
            } else if (16 == subSquare) {
                result = "q";
            } else if (17 == subSquare) {
                result = "r";
            } else if (18 == subSquare) {
                result = "s";
            } else if (19 == subSquare) {
                result = "t";
            } else if (20 == subSquare) {
                result = "u";
            } else if (21 == subSquare) {
                result = "v";
            } else if (22 == subSquare) {
                result = "w";
            } else if (23 == subSquare) {
                result = "x";
            } else {
                throw new IllegalStateException();
            }
        } catch (IllegalStateException e) {
            MyExceptionHandler exceptionHandler = new MyExceptionHandler();
            exceptionHandler.pushExceptionMessage("Error in calculating sub-square grid");
            result = "ø";
        }

        return result;
    }

}
