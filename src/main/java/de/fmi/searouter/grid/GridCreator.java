package de.fmi.searouter.grid;

import de.fmi.searouter.domain.CoastlineWay;
import de.fmi.searouter.utils.IntersectionHelper;
import de.fmi.searouter.osmimport.CoastlineImporterMoreEfficient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class GridCreator {

    protected static List<GridNode> gridNodes;

    public static Map<Double, Map<Double, GridNode>> coordinateNodeStore;


    private static final int DIMENSION_LATITUDE = 500;
    private static final int DIMENSION_LONGITUDE = 2000;

    public static double coordinate_step_latitude;
    public static double coordinate_step_longitude;

    private static List<BevisChatelainInPolygonCheck> checkerObjects;

    public static void initCheckObjs(List<CoastlineWay> coastlinePolygons) {
        checkerObjects = new ArrayList<>();
        for (CoastlineWay polygon : coastlinePolygons) {
            checkerObjects.add(new BevisChatelainInPolygonCheck(polygon));
        }
    }

    public static boolean isPointOnWater(double latitude, double longitude) {
        if (latitude > -52) {
            return true;
        }

        for (BevisChatelainInPolygonCheck checkerObj : checkerObjects) {
            if (!checkerObj.isPointInWater(latitude, longitude)) {
                System.out.println(latitude + " " + longitude + " is on land");
                return false;
            }
        }
        System.out.println(latitude + " " + longitude + " is on water");
        return true;
    }


    public static void createGrid(List<CoastlineWay> coastlinePolygons) throws InterruptedException {
        gridNodes = new ArrayList<>();

        coordinateNodeStore = new HashMap<>();

        coordinate_step_latitude = (double) 180 / DIMENSION_LATITUDE;
        coordinate_step_longitude = (double) 360 / DIMENSION_LONGITUDE;
        System.out.println(coordinate_step_latitude);
        System.out.println(coordinate_step_longitude);


        initCheckObjs(coastlinePolygons);

        BigDecimal coordinateStepLat = BigDecimal.valueOf(coordinate_step_latitude);
        BigDecimal coordinateStepLong = BigDecimal.valueOf(coordinate_step_longitude);

        BigDecimal latEnd = BigDecimal.valueOf(-90);
        BigDecimal longEnd = BigDecimal.valueOf(-180);

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

    private static GridNode getNodeByLatLong(double latitude, double longitude) {
        if (coordinateNodeStore.containsKey(latitude) && coordinateNodeStore.get(latitude).containsKey(longitude)) {
            return coordinateNodeStore.get(latitude).get(longitude);
        }
        return null;
    }

    public static void main(String[] args) {


        // Import coastlines
        CoastlineImporterMoreEfficient importer = new CoastlineImporterMoreEfficient();
        List<CoastlineWay> coastlines = new ArrayList<>();


        try {
            //coastlines = importer.importPBF("planet-coastlinespbf-cleaned.pbf");
            coastlines = importer.importPBF("antarctica-latest.osm.pbf");
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            GridCreator.createGrid(coastlines);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
