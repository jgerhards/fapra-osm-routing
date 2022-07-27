package de.fmi.searouter.dijkstragrid;

import com.google.common.math.DoubleMath;
import de.fmi.searouter.coastlinegrid.CoastlineChecker;
import de.fmi.searouter.coastlinegrid.CoastlineWays;
import de.fmi.searouter.importdata.CoastlineWay;
import de.fmi.searouter.utils.GeoJsonConverter;
import de.fmi.searouter.utils.IntersectionHelper;
import de.fmi.searouter.osmimport.CoastlineImporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Contains logic for creating a grid graph which nodes are distributed equally over the latitude
 * and longitudes of a world map.
 */
public class GridCreator {

    protected static List<GridNode> gridNodes;

    // Used for creating the grid. <Latitude, <Longitude, GridNode>>
    public static Map<Double, Map<Double, GridNode>> coordinateNodeStore;

    // Resolution of the grid
    private static final int DIMENSION_LATITUDE = 500;
    private static final int DIMENSION_LONGITUDE = 2000;

    // Difference between latitude/longitude coordinates between two neighbor grid nodes
    public static double coordinate_step_latitude;
    public static double coordinate_step_longitude;

    // All polygons have a point-in-polygon check object
    private static CoastlineChecker coastlineChecker;

    /**
     * Checks whether a given coordinate point is on water or on land.
     *
     * @param latitude The latitude of point P to check.
     * @param longitude The longitude of point P to check.
     * @return True: Point on water, False: Water on land
     */
    public static boolean isPointOnWater(double latitude, double longitude) {
        if (coastlineChecker.pointInWater((float) latitude, (float) longitude)) {
            return true;
        }
        return false;
    }

    /**
     * Creates the grid graph for the Dijkstra routing. Fills the {@link Grid}, {@link Node} and {@link Edge}
     * data structures.
     *
     * @param coastlinePolygons All coastline polygons represented as {@link CoastlineWay} that should be considered
     *                          for a point-in-polygon check
     * @throws InterruptedException If something with the threads went wrong
     */
    public static void createGrid() throws InterruptedException {
        gridNodes = new ArrayList<>();

        coordinateNodeStore = new HashMap<>();

        coordinate_step_latitude = (double) 180 / DIMENSION_LATITUDE;
        coordinate_step_longitude = (double) 360 / DIMENSION_LONGITUDE;
        System.out.println(coordinate_step_latitude);
        System.out.println(coordinate_step_longitude);

        BigDecimal coordinateStepLat = BigDecimal.valueOf(coordinate_step_latitude);

        BigDecimal latEnd = BigDecimal.valueOf(-90);

        // Precalculate all latitudes that should be checked
        int numberOfThreads = 15;
        List<NodeCreateWorkerThread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new NodeCreateWorkerThread());
        }

        int count = 0;
        for (BigDecimal lat = BigDecimal.valueOf(90.0); lat.compareTo(latEnd) >= 0; lat = lat.subtract(coordinateStepLat)) {
            int threadToAssign = count % numberOfThreads;
            threads.get(threadToAssign).addLatitude(lat);
            count++;
        }

        for (NodeCreateWorkerThread n : threads) {
            n.start();
        }

        for (NodeCreateWorkerThread n : threads) {
            n.join();
        }

        // Create Node arrays
        double[] latitude = new double[gridNodes.size()];
        double[] longitude = new double[gridNodes.size()];

        // Assign ids to nodes and fill up Node data structure
        for (int id = 0; id < gridNodes.size(); id++) {
            gridNodes.get(id).setId(id);
            latitude[id] = gridNodes.get(id).getLatitude();
            longitude[id] = gridNodes.get(id).getLongitude();
        }

        // Dynamic Edge info
        List<Integer> dynamicStartNode = new ArrayList<>();
        List<Integer> dynamicDestNode = new ArrayList<>();
        List<Integer> dynamicDist = new ArrayList<>();

        int[] offsets = new int[gridNodes.size() + 1];

        // For every node, check if neighbour nodes are existing and if yes add it as edge
        for (int nodeIdx = 0; nodeIdx < gridNodes.size(); nodeIdx++) {
            GridNode currNode = gridNodes.get(nodeIdx);

            // Calculate the lat/longs where a neighbour node should be. The Objects do not belong to the grid
            GridNode eastCalcNode = currNode.calcEasternNode(coordinate_step_longitude);
            GridNode westCalcNode = currNode.calcWesternNode(coordinate_step_longitude);
            GridNode northCalcNode = currNode.calcNorthernNode(coordinate_step_latitude);
            GridNode southCalcNode = currNode.calcSouthernNode(coordinate_step_latitude);

            // Init the real GridNode objects which are known by the current grid
            GridNode east = null;
            GridNode west = null;
            GridNode north = null;
            GridNode south = null;

            // Check if the calculated lat/longs of neighbor nodes are actually real existing water nodes in the grid
            if (eastCalcNode != null) {
                east = getNodeByLatLong(eastCalcNode.getLatitude(), eastCalcNode.getLongitude());
            }
            if (westCalcNode != null) {
                west = getNodeByLatLong(westCalcNode.getLatitude(), westCalcNode.getLongitude());
            }
            if (northCalcNode != null) {
                north = getNodeByLatLong(northCalcNode.getLatitude(), northCalcNode.getLongitude());
            }
            if (southCalcNode != null) {
                south = getNodeByLatLong(southCalcNode.getLatitude(), southCalcNode.getLongitude());
            }

            List<GridNode> neighbourNodes = Arrays.asList(east, west, north, south);

            // Update the offset according to the number of edges
            if (nodeIdx == 0) {
                offsets[nodeIdx] = 0;
            } else {
                offsets[nodeIdx] = dynamicStartNode.size();
            }

            // For all existing neighbour nodes: Add the information to the dynamic edge list
            for (GridNode node : neighbourNodes) {
                if (node != null) {
                    dynamicStartNode.add(nodeIdx);
                    dynamicDestNode.add(node.getId());
                    dynamicDist.add((int) IntersectionHelper.getDistance(
                            currNode.getLatitude(), currNode.getLongitude(),
                            node.getLatitude(), node.getLongitude())
                    );
                }
            }
        }

        offsets[offsets.length - 1] = dynamicStartNode.size();

        // Convert dynamic Edge data structures to static arrays
        int[] startNode = new int[dynamicStartNode.size()];
        int[] destNode = new int[dynamicDestNode.size()];
        int[] dist = new int[dynamicDist.size()];
        for (int i = 0; i < startNode.length; i++) {
            startNode[i] = dynamicStartNode.get(i);
            destNode[i] = dynamicDestNode.get(i);
            dist[i] = dynamicDist.get(i);
        }

        // Fill the Node and Edge classes
        Node.setLatitude(latitude);
        Node.setLongitude(longitude);
        Edge.setStartNode(startNode);
        Edge.setDestNode(destNode);
        Edge.setDist(dist);


        try {
            Grid.exportToFmiFile("exported_grid.fmi");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Finds a known {@link GridNode} by its coordinates.
     *
     * @param latitude The latitude coordinate of the GridNode to search for
     * @param longitude The longitude coordinate of the GridNode to search for.
     * @return The searched {@link GridNode} object or null if not found
     */
    private static GridNode getNodeByLatLong(double latitude, double longitude) {
        if (coordinateNodeStore.containsKey(latitude) && coordinateNodeStore.get(latitude).containsKey(longitude)) {
            return coordinateNodeStore.get(latitude).get(longitude);
        }
        return null;
    }

    /**
     * Entry point for the pre-processing part of this project.
     * @param args Not used
     */
    public static void main(String[] args) throws IOException {

        boolean test = IntersectionHelper.arcsIntersect(5.0, 10.0000, 6.0, 10.0,
                4.0, 10.0, 9.0, 10.0);
        test = IntersectionHelper.crossesLatitude(52.0371, 	-174.9236, 52.0367, 	-174.9236,
                52.03703703703704, -175.0, -174.87654320987656);
        System.out.println(test);
        test = IntersectionHelper.crossesLatitude(-53.518513, 	-73.53756,-53.518517, 	-73.53756,
                -53.51851851851852, -73.64197530864197, -73.5185185185185);
        System.out.println(test);
        //System.exit(0);

        Date startTime = new Date();
        if(false) {
            // Import coastlines
            CoastlineImporter importer = new CoastlineImporter();
            List<CoastlineWay> coastlines = new ArrayList<>();

            try {
                //coastlines = importer.importPBF("planet-coastlines.pbf");
                //coastlines = importer.importPBF("antarctica-latest.osm.pbf");
                //coastlines = importer.importPBF("south-america-latest.osm.pbf");
                coastlines = importer.importPBF("planet-coastlinespbf-cleaned.pbf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            CoastlineWays.initEdges(coastlines);
            coastlines = null;
            CoastlineWays.storeData();
        } else {
            CoastlineWays.getData();
        }

        System.out.println(CoastlineWays.getNumberOfEdges());

        /*test = IntersectionHelper.crossesLatitude(CoastlineWays.getStartLatByEdgeIdx(30875904), CoastlineWays.getStartLonByEdgeIdx(30875904),
                CoastlineWays.getDestLatByEdgeIdx(30875904), CoastlineWays.getDestLonByEdgeIdx(30875904),
                55.0, -163.9566832780838, -163.8888888888889);
        System.out.println(test);
        System.exit(0);*/

        /*int numOfEdges = CoastlineWays.getNumberOfEdges();
        for(int i = 0; i < numOfEdges; i++) {
            if(DoubleMath.fuzzyEquals(CoastlineWays.getStartLatByEdgeIdx(i), 			55.0001891, 0.0001) &&
                    DoubleMath.fuzzyEquals(CoastlineWays.getStartLonByEdgeIdx(i), 		-163.9566860, 0.0001) &&
                    DoubleMath.fuzzyEquals(CoastlineWays.getDestLatByEdgeIdx(i), 	55.0000002, 0.0001) &&
                    DoubleMath.fuzzyEquals(CoastlineWays.getDestLonByEdgeIdx(i), 			-163.9570449, 0.0001)
            ) {
                System.out.println("ppp: found " + i);
            } else if(DoubleMath.fuzzyEquals(CoastlineWays.getDestLatByEdgeIdx(i), 	55.0000002, 0.0001) &&
                    DoubleMath.fuzzyEquals(CoastlineWays.getDestLonByEdgeIdx(i), 			-163.9570449, 0.0001) &&
                    DoubleMath.fuzzyEquals(CoastlineWays.getStartLatByEdgeIdx(i), 		55.0001891, 0.0001) &&
                    DoubleMath.fuzzyEquals(CoastlineWays.getStartLonByEdgeIdx(i), 		-163.9566860, 0.0001)
            ) {
                System.out.println("ppp: found" + i);
            }
        }
        System.exit(0);/*
       /* Map<Float, Map<Float, List<Float[]>>> checkerMap = new HashMap();
        int numOfEdges = CoastlineWays.getNumberOfEdges();
        for(int i = 0; i < numOfEdges; i++) {
            Float values[] = new Float[]{CoastlineWays.getStartLatByEdgeIdx(i),
                    CoastlineWays.getStartLonByEdgeIdx(i)};
            if(checkerMap.containsKey(values[0])) {
                if(checkerMap.get(values[0]).containsKey(values[1])) {
                    Float valuesDest[] = new Float[]{CoastlineWays.getDestLatByEdgeIdx(i),
                            CoastlineWays.getDestLonByEdgeIdx(i)};
                    for(Float[] arr : checkerMap.get(values[0]).get(values[1])) {
                        if(arr[0].equals(valuesDest[0]) && arr[1].equals(valuesDest[1])) {
                            System.out.println("equal pair found");
                            System.exit(0);
                        }
                    }
                } else {
                    checkerMap.get(values[0]).put(values[1], new ArrayList<>());
                    Float valuesDest[] = new Float[]{CoastlineWays.getDestLatByEdgeIdx(i),
                            CoastlineWays.getDestLonByEdgeIdx(i)};
                    checkerMap.get(values[0]).get(values[1]).add(valuesDest);
                }
            } else {
                checkerMap.put(values[0], new HashMap<>());
                checkerMap.get(values[0]).put(values[1], new ArrayList<>());
                Float valuesDest[] = new Float[]{CoastlineWays.getDestLatByEdgeIdx(i),
                        CoastlineWays.getDestLonByEdgeIdx(i)};
                checkerMap.get(values[0]).get(values[1]).add(valuesDest);
            }


            values = new Float[]{CoastlineWays.getDestLatByEdgeIdx(i),
                    CoastlineWays.getDestLonByEdgeIdx(i)};
            if(checkerMap.containsKey(values[0])) {
                if(checkerMap.get(values[0]).containsKey(values[1])) {
                    Float valuesDest[] = new Float[]{CoastlineWays.getStartLatByEdgeIdx(i),
                            CoastlineWays.getStartLonByEdgeIdx(i)};
                    for(Float[] arr : checkerMap.get(values[0]).get(values[1])) {
                        if(arr[0].equals(valuesDest[0]) && arr[1].equals(valuesDest[1])) {
                            System.out.println("equal pair found");
                            System.exit(0);
                        }
                    }
                } else {
                    checkerMap.get(values[0]).put(values[1], new ArrayList<>());
                    Float valuesDest[] = new Float[]{CoastlineWays.getDestLatByEdgeIdx(i),
                            CoastlineWays.getDestLonByEdgeIdx(i)};
                    checkerMap.get(values[0]).get(values[1]).add(valuesDest);
                }
            } else {
                checkerMap.put(values[0], new HashMap<>());
                checkerMap.get(values[0]).put(values[1], new ArrayList<>());
                Float valuesDest[] = new Float[]{CoastlineWays.getDestLatByEdgeIdx(i),
                        CoastlineWays.getDestLonByEdgeIdx(i)};
                checkerMap.get(values[0]).get(values[1]).add(valuesDest);
            }
        }*/
        coastlineChecker = CoastlineChecker.getInstance();

        for(int latIdx = 0; latIdx < 18; latIdx++) {
            List<GridNode> centerPoints = CoastlineChecker.getInstance().getAllCenterPoints(latIdx);

            String json = GeoJsonConverter.osmNodesToGeoJSON(centerPoints).toString(1);
            String fileName = "centerpoints" + latIdx + ".json";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(json);
            writer.close();
        }

        /*String json2 = GeoJsonConverter.coastlineWayToGeoJSON(coastlines).toString(1);
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("ways.json"));
        writer2.write(json2);
        writer2.close();*/

        /*String json3 = GeoJsonConverter.coastlineWaysToGeoJSON().toString(0);
        BufferedWriter writer3 = new BufferedWriter(new FileWriter("ways2.json"));
        writer3.write(json3);
        writer3.close();*/

        try {
            GridCreator.createGrid();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Date endTime = new Date();
        long timeDiffMin = ((endTime.getTime() - startTime.getTime())/1000)/60;
        System.out.println("ttt: runtime: " + timeDiffMin);

    }


}
