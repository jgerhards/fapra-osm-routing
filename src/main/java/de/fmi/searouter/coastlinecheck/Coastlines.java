package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.domain.CoastlineWay;
import de.fmi.searouter.domain.IntersectionHelper;
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
    public static void initCoastlines(List<CoastlineWay> coastlinesToImport) {

        // As we yet do not know the array sizes of the array data structures we
        // first add all data to dynamic data structures and then make an array in the end out of it
        List<Integer> dynamicStartIds = new ArrayList<>();
        List<Integer> dynamicEndIds = new ArrayList<>();

        List<Double> dynamicLength = new ArrayList<>();

        List<Double> dynamicNodeLongitude = new ArrayList<>();
        List<Double> dynamicNodeLatitude = new ArrayList<>();

        // Number of all coastline ways
        numbersOfCoastlineWays = 0;
        // Counter to keep track of the start and end ids of a way
        int coastLineWayIdCounter = 0;

        for (int coastlineIdx = 0; coastlineIdx < coastlinesToImport.size(); coastlineIdx++) {
            CoastlineWay currCoastline = coastlinesToImport.get(coastlineIdx);
            List<WayNode> currWayNodes = currCoastline.getWayNodes();

            for (int wayNodeIdx = 1; wayNodeIdx < currWayNodes.size(); wayNodeIdx++) {
                // Add each edge of the CoastlineWayPolygon as a single way to the dynamic data structures

                // Start point of edge
                dynamicStartIds.add(coastLineWayIdCounter);
                double pointALatitude = currWayNodes.get(wayNodeIdx-1).getLatitude();
                double pointALongitude = currWayNodes.get(wayNodeIdx-1).getLongitude();

                dynamicNodeLatitude.add(pointALatitude);
                dynamicNodeLongitude.add(pointALongitude);

                coastLineWayIdCounter += 1;
                dynamicEndIds.add(coastLineWayIdCounter);

                // End point of edge
                double pointBLatitude = currWayNodes.get(wayNodeIdx).getLatitude();
                double pointBLongitude = currWayNodes.get(wayNodeIdx).getLongitude();

                dynamicNodeLatitude.add(pointBLatitude);
                dynamicNodeLongitude.add(pointBLongitude);

                // Length of edge
                dynamicLength.add(IntersectionHelper.getDistance(
                        pointALatitude, pointALongitude,
                        pointBLatitude, pointBLongitude)
                );

                numbersOfCoastlineWays++;
            }
            // A new polygon begins in the next iteration, therefore the next start id should not be equal to the last end id
            coastLineWayIdCounter++;
        }


        // Copy the latitude and longitude info to the more efficient array data structures
        nodeLongitude = new double[dynamicNodeLatitude.size()];
        nodeLatitude = new double[dynamicNodeLongitude.size()];
        for (int i = 0; i < dynamicNodeLatitude.size(); i++) {
            nodeLongitude[i] = dynamicNodeLatitude.get(i);
            nodeLatitude[i] = dynamicNodeLongitude.get(i);
        }

        // Copy all dynamic list structures which have the size of numbersOfCoastlineWays to arrays
        length = new double[numbersOfCoastlineWays];
        startID = new int[numbersOfCoastlineWays];
        endID = new int[numbersOfCoastlineWays];
        for (int i = 0; i < numbersOfCoastlineWays; i++) {
            length[i] = dynamicLength.get(i);
            startID[i] = dynamicStartIds.get(i);
            endID[i] = dynamicEndIds.get(i);
        }
    }
}
