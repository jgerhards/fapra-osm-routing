package de.fmi.searouter.coastlinecheck;

/**
 * class used to store longitudes and latitudes of start and end points of coastline ways. Stored as
 * an array of primitives due to lookup being faster this way.
 */
public class Coastlines {
    //the number of coastline ways
    private static int numbersOfCoastlineWays;

    //for every coastline way (ID is the index in these two arrays), the start and end node.
    private static int[] startID;
    private static int[] endID;

    //for every coastline way, the length is stored to speed up some operations
    private static double[] length;

    //for every node (ID is the index in these two arrays), longitude and latitude.
    private static double[] nodeLongitude;
    private static double[] nodeLatitude;

    //global reference point for checking if other points are in water or on land
    //todo: find an appropriate point
    public static final double GLOBAL_REFERENCE_LATITUDE = 0.0;
    public static final double GLOBAL_REFERENCE_LONGITUDE = 0.0;
    public static final boolean GLOBAL_REFERENCE_IN_WATER = true;

    /**
     * gets the number of coastline ways
     * @return the number of coastline ways
     */
    public static int getNumberOfWays() {
        return numbersOfCoastlineWays;
    }

    /**
     * gets the length of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the length of the requested coastline way.
     */
    public static double getLength(int coastlineID) {
        return length[coastlineID];
    }

    /**
     * gets the longitude of the start node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the longitude of the start point of the requested coastline way.
     */
    public static double getStartLongitude(int coastlineID) {
        int longArrayID = startID[coastlineID];
        return nodeLongitude[longArrayID];
    }

    /**
     * gets the latitude of the start node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the latitude of the start point of the requested coastline way.
     */
    public static double getStartLatitude(int coastlineID) {
        int latArrayID = startID[coastlineID];
        return nodeLatitude[latArrayID];
    }

    /**
     * gets the longitude of the end node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the longitude of the end point of the requested coastline way.
     */
    public static double getEndLongitude(int coastlineID) {
        int longArrayID = endID[coastlineID];
        return nodeLongitude[longArrayID];
    }

    /**
     * gets the latitude of the end node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the latitude of the end point of the requested coastline way.
     */
    public static double getEndLatitude(int coastlineID) {
        int latArrayID = endID[coastlineID];
        return nodeLatitude[latArrayID];
    }
}
