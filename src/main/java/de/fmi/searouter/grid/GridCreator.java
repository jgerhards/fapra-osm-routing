package de.fmi.searouter.grid;

import de.fmi.searouter.domain.BevisChatelainCoastlineCheck;
import de.fmi.searouter.domain.CoastlineWay;
import de.fmi.searouter.domain.IntersectionHelper;
import de.fmi.searouter.osmexport.GeoJsonConverter;
import de.fmi.searouter.osmimport.CoastlineImporter;
import de.fmi.searouter.osmimport.CoastlineImporterMoreEfficient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class GridCreator {

    private static List<GridNode> gridNodes;

    private static Map<Double, Map<Double, GridNode>> coordinateNodeStore;


    private static final int DIMENSION_LATITUDE = 500;
    private static final int DIMENSION_LONGITUDE = 2000;

    public static double coordinate_step_latitude;
    public static double coordinate_step_longitude;

    public static boolean isPointOnWater(double latitude, double longitude, List<CoastlineWay> coastlinePolygons) {
        for (CoastlineWay polygon : coastlinePolygons) {
            BevisChatelainCoastlineCheck check = new BevisChatelainCoastlineCheck(polygon);
            if (!check.isPointInWater(latitude, longitude)) {
                System.out.println(latitude + " " + longitude + " is on land");
                return false;
            }
        }
        System.out.println(latitude + " " + longitude + " is on water");
        return true;
    }


    public static void createGrid(List<CoastlineWay> coastlinePolygons) {
        gridNodes = new ArrayList<>();

        coordinateNodeStore = new HashMap<>();

        coordinate_step_latitude = (double) 180 / DIMENSION_LATITUDE;
        coordinate_step_longitude = (double) 360 / DIMENSION_LONGITUDE;
        System.out.println(coordinate_step_latitude);
        System.out.println(coordinate_step_longitude);

        /* Double variant
        for (double latitude = -90.0; latitude <= 90.0; latitude = latitude + coordinate_step_latitude) {
            for (double longitude = -180.0; longitude <= 180.0; longitude = longitude + coordinate_step_longitude) {
                GridNode node = new GridNode(latitude, longitude);
                gridNodes.add(node);
            }
        }
        */

        BigDecimal coordinateStepLat = BigDecimal.valueOf(coordinate_step_latitude);
        BigDecimal coordinateStepLong = BigDecimal.valueOf(coordinate_step_longitude);

        BigDecimal latEnd = BigDecimal.valueOf(-90);
        BigDecimal longEnd = BigDecimal.valueOf(-180);
        for (BigDecimal lat = BigDecimal.valueOf(90.0); lat.compareTo(latEnd) >= 0; lat = lat.subtract(coordinateStepLat)) {
            for (BigDecimal longitude = BigDecimal.valueOf(180); longitude.compareTo(longEnd) > 0; longitude = longitude.subtract(coordinateStepLong)) {

                if (isPointOnWater(lat.doubleValue(), longitude.doubleValue(), coastlinePolygons)) {
                    continue;
                }

                GridNode node = new GridNode(lat.doubleValue(), longitude.doubleValue());

                gridNodes.add(node);
                if (!coordinateNodeStore.containsKey(lat.doubleValue())) {
                    coordinateNodeStore.put(lat.doubleValue(), new HashMap<>());
                }
                coordinateNodeStore.get(lat.doubleValue()).put(longitude.doubleValue(), node);
            }
        }

        // TODO Filter land nodes (e.g. in previous step)
        ;

        // FOr test TODO REMOVE
        //gridNodes = new ArrayList<>();
        /*
        gridNodes = Arrays.asList(
                getNodeByLatLong(0.0, 0.0),
                getNodeByLatLong(0.0, 0.18),
                getNodeByLatLong(0.0, -0.18),
                getNodeByLatLong(0.36, 0.0),
                getNodeByLatLong(-0.36, 0.0),
                getNodeByLatLong(-0.72, 0.0)
        );

        coordinateNodeStore = new HashMap<>();
        Map<Double, GridNode> zeroHash = new HashMap<>();
        zeroHash.put(0.0, gridNodes.get(0));
        zeroHash.put(0.18, gridNodes.get(1));
        zeroHash.put(-0.18, gridNodes.get(2));
        coordinateNodeStore.put(0.0, zeroHash);

        Map<Double, GridNode> zerothreesix = new HashMap<>();
        zerothreesix.put(0.0, gridNodes.get(3));
        coordinateNodeStore.put(0.36, zerothreesix);

        Map<Double, GridNode> mzerothreesix = new HashMap<>();
        mzerothreesix.put(0.0, gridNodes.get(4));
        coordinateNodeStore.put(-0.36, mzerothreesix);

        Map<Double, GridNode> mseventwo = new HashMap<>();
        mseventwo.put(0.0, gridNodes.get(5));
        coordinateNodeStore.put(-0.72, mseventwo);
        // TODO TEST END
        */

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

        // TEST DATA
        /*
        GridNode center = coordinateNodeStore.get(0.0).get(0.0);
        GridNode east = coordinateNodeStore.get(0.0).get(0.0).calcEasternNode(coordinate_step_longitude);
        GridNode west = coordinateNodeStore.get(0.0).get(0.0).calcWesternNode(coordinate_step_longitude);
        GridNode north = coordinateNodeStore.get(0.0).get(0.0).calcNorthernNode(coordinate_step_latitude);
        GridNode south = coordinateNodeStore.get(0.0).get(0.0).calcSouthernNode(coordinate_step_latitude);

        List<GridNode> nodes = Arrays.asList(center, east, west, north, south);

        System.out.println(GeoJsonConverter.osmNodesToGeoJSON(gridNodes).toString(1));


        System.out.println(gridNodes.size());
        */

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
            coastlines = importer.importPBF("planet-coastlinespbf-cleaned.pbf");
            //coastlines = importer.importPBF("antarctica-latest.osm.pbf");
        } catch (IOException e) {
            e.printStackTrace();
        }


        GridCreator.createGrid(coastlines);
    }


}
