package de.fmi.searouter.grid;

import de.fmi.searouter.osmexport.GeoJsonConverter;

import java.math.BigDecimal;
import java.util.*;

public class GridCreator {

    private static List<GridNode> gridNodes;

    private static Map<Double, Map<Double, GridNode>> coordinateNodeStore;

    private static final int DIMENSION_LATITUDE = 500;
    private static final int DIMENSION_LONGITUDE = 2000;

    public static double coordinate_step_latitude;
    public static double coordinate_step_longitude;

    public static void createGrid() {
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
                GridNode node = new GridNode(lat.doubleValue(), longitude.doubleValue());
                gridNodes.add(node);
                if (!coordinateNodeStore.containsKey(lat.doubleValue())) {
                    coordinateNodeStore.put(lat.doubleValue(), new HashMap<>());
                }
                coordinateNodeStore.get(lat.doubleValue()).put(longitude.doubleValue(), node);
            }
        }

        // Filter land nodes
        ;

        // Assign ids to nodes
        for (int id = 0; id < gridNodes.size(); id++) {
            gridNodes.get(id).setId(id);
        }

        GridNode center = coordinateNodeStore.get(0.0).get(0.0);
        GridNode east = coordinateNodeStore.get(0.0).get(0.0).calcEasternNode(coordinate_step_longitude);
        GridNode west = coordinateNodeStore.get(0.0).get(0.0).calcWesternNode(coordinate_step_longitude);
        GridNode north = coordinateNodeStore.get(0.0).get(0.0).calcNorthernNode(coordinate_step_latitude);
        GridNode south = coordinateNodeStore.get(0.0).get(0.0).calcSouthernNode(coordinate_step_latitude);

        List<GridNode> nodes = Arrays.asList(center, east, west, north, south);

        System.out.println(GeoJsonConverter.osmNodesToGeoJSON(nodes).toString(1));


        System.out.println(gridNodes.size());

    }

    public static void main(String[] args) {
        GridCreator.createGrid();
    }


}
