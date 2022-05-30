package de.fmi.searouter.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {

    private static double[] latitude;
    private static double[] longitude;

    public static double getLatitude(int i) {
        return latitude[i];
    }

    public static double getLongitude(int i) {
        return longitude[i];
    }

    public static void setLatitude(double[] latitude) {
        Node.latitude = latitude;
    }

    public static void setLongitude(double[] longitude) {
        Node.longitude = longitude;
    }

    public static int getSize() {
        return Node.latitude.length;
    }


}
