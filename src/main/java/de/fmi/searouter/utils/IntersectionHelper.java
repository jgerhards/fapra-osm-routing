package de.fmi.searouter.utils;

import java.util.Arrays;

/**
 * Geometrical spheric calculation helper methods.
 */
public class IntersectionHelper {
    //earths radius is required for distance calculation
    private static final double EARTH_RADIUS = 6371000.0;

    /**
     * Calculates the distance between two points basted on latitude and longitude.
     * Formulas used can be found here: http://www.movable-type.co.uk/scripts/latlong.html
     * @param startLat latitude of the first point
     * @param startLong longitude of the first point
     * @param endLat latitude of the second point
     * @param endLong longitude of the second point
     * @return the distance between fist and second point in meters
     */
    public static double getDistance(double startLat, double startLong, double endLat, double endLong) {
        double radianStartLat = convertToRadian(startLat);
        double radianEndLat = convertToRadian(endLat);
        double latDifference = convertToRadian(endLat - startLat);
        double longDifference = convertToRadian(endLong - startLong);

        double haversine = Math.sin(latDifference / 2) * Math.sin(latDifference / 2) +
                Math.cos(radianStartLat) * Math.cos(radianEndLat) * Math.sin(longDifference / 2) *
                        Math.sin(longDifference / 2);
        double c = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

        double distanceInMeters = c * EARTH_RADIUS;
        return distanceInMeters;
    }

    /**
     * converts a coordinate to a radian representation
     * @param coordinate
     * @return
     */
    private static double convertToRadian(double coordinate) {
        return coordinate * (Math.PI / 180.0);
    }
}
