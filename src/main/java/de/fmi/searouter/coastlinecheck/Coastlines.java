package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.domain.CoastlineWay;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.ArrayList;
import java.util.List;

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
        return 0.0;
    }

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


    /**
     * Initializes this static coastline store class by importing {@link CoastlineWay} objects to fill up
     * the data structures of this class.
     *
     * @param coastlinesToImport The coastlines which should be transformed to the data structures in this class.
     */
    public void initCoastlines(List<CoastlineWay> coastlinesToImport) {
        // Set the number of all coastlines
        numbersOfCoastlineWays = coastlinesToImport.size();

        startID = new int[numbersOfCoastlineWays];
        endID = new int[numbersOfCoastlineWays];

        length = new double[numbersOfCoastlineWays];

        // As we yet do not know the array sizes of the nodeLongitude and nodeLatitude array, we
        // first add all data to dynamic data structures and then make an array in the end out of it
        List<Double> nodeLongitudes = new ArrayList<>();
        List<Double> nodeLatitudes = new ArrayList<>();

        for (int coastlineIdx = 0; coastlineIdx < coastlinesToImport.size(); coastlineIdx++) {
            CoastlineWay currCoastline = coastlinesToImport.get(coastlineIdx);
            List<WayNode> currWayNodes = currCoastline.getWayNodes();

            // Setup length
            length[coastlineIdx] = currCoastline.getLength();


            // Setup startID and endID as well as preparing temporary dynamic data structures for the latitude and longitude arrays
            startID[coastlineIdx] = nodeLongitudes.size();
            // Add way nodes to lists
            for (int wayNodeIdx = 0; wayNodeIdx < currWayNodes.size(); wayNodeIdx++) {
                WayNode currWayNode = currWayNodes.get(wayNodeIdx);

                nodeLongitudes.add(currWayNode.getLongitude());
                nodeLatitudes.add(currWayNode.getLatitude());
            }
            endID[coastlineIdx] = nodeLongitudes.size() - 1;
        }

        // Copy the latitude and longitude info to the more efficient array data structures
        nodeLongitude = new double[nodeLongitudes.size()];
        nodeLatitude = new double[nodeLatitudes.size()];
        for (int i = 0; i < nodeLatitudes.size(); i++) {
            nodeLongitude[i] = nodeLongitudes.get(i);
            nodeLatitude[i] = nodeLatitudes.get(i);
        }
    }
}
