package de.fmi.searouter.domain;

/**
 * class used to store longitudes and latitudes of start and end points of coastline ways. Stored as
 * an array of primitives due to lookup being faster this way.
 */
public class Coastlines {
    //for every coastline way (ID is the index in these two arrays), the start and end node.
    private static int[] startID;
    private static int[] endID;

    //for every node (ID is the index in these two arrays), longitude and latitude.
    private static double[] nodeLongitude;
    private static double[] nodeLatitude;

    /**
     * gets the longitude of the start node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the longitude of the start point of the requested coastline way.
     */
    public static double getStartLongitude(int coastlineID) {
        return 0.0;
    }

    /**
     * gets the latitude of the start node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the latitude of the start point of the requested coastline way.
     */
    public static double getStartLatitude(int coastlineID) {
        return 0.0;
    }

    /**
     * gets the longitude of the end node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the longitude of the end point of the requested coastline way.
     */
    public static double getEndLongitude(int coastlineID) {
        return 0.0;
    }

    /**
     * gets the latitude of the end node of a given coastline way.
     * @param coastlineID the ID of the coastline way.
     * @return the latitude of the end point of the requested coastline way.
     */
    public static double getEndLatitude(int coastlineID) {
        return 0.0;
    }
}
