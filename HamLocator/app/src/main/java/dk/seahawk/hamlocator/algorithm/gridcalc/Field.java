package dk.seahawk.hamlocator.algorithm.gridcalc;

import dk.seahawk.hamlocator.util.MyExceptionHandler;

public class Field {

    public Field() {
    }

    public String getField(int longitudeField, int latitudeField) {
        return getLocationField(longitudeField) + getLocationField(latitudeField);
    }

    private String getLocationField(int field) {
        String result;

        try {
            if (0 == field) {
                result = "A";
            } else if (1 == field) {
                result = "B";
            } else if (2 == field) {
                result = "C";
            } else if (3 == field) {
                result = "D";
            } else if (4 == field) {
                result = "E";
            } else if (5 == field) {
                result = "F";
            } else if (6 == field) {
                result = "G";
            } else if (7 == field) {
                result = "H";
            } else if (8 == field) {
                result = "I";
            } else if (9 == field) {
                result = "J";
            } else if (10 == field) {
                result = "K";
            } else if (11 == field) {
                result = "L";
            } else if (12 == field) {
                result = "M";
            } else if (13 == field) {
                result = "N";
            } else if (14 == field) {
                result = "O";
            } else if (15 == field) {
                result = "P";
            } else if (16 == field) {
                result = "Q";
            } else if (17 == field) {
                result = "R";
            } else {
                throw new IllegalStateException();
            }
        } catch (IllegalStateException e) {
            MyExceptionHandler exceptionHandler = new MyExceptionHandler();
            exceptionHandler.pushExceptionMessage("Error in calculating field grid");
            result = "Ø";
        }

        return result;
    }

}
