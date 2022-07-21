package de.fmi.searouter.utils;

import java.util.Arrays;

/**
 * Geometrical spheric calculation helper methods.
 */
public class IntersectionHelper {

    public static boolean[] getPositionInfoOfPointRelativeToCellRough(double pointToCheckLat, double pointToCheckLon,
                                                                      double leftBoundLon, double rightBoundLon,
                                                                      double lowerBoundLat, double upperBoundLat) {
        if (Math.abs(pointToCheckLat - lowerBoundLat) > 11.0 && Math.abs(pointToCheckLat - upperBoundLat) > 11.0) {
            return new boolean[]{true, true, true, true};
        } else if (Math.abs(pointToCheckLon - leftBoundLon) > 11.0 && Math.abs(pointToCheckLon - rightBoundLon) > 11.0) {
            return new boolean[]{true, true, true, true};
        } else {
            return getPositionInfoOfPointRelativeToCell(pointToCheckLat, pointToCheckLon,
                    leftBoundLon, rightBoundLon, lowerBoundLat, upperBoundLat);
        }
    }

    /**
     * @param pointToCheckLat
     * @param pointToCheckLon
     * @param leftBoundLon
     * @param rightBoundLon
     * @param lowerBoundLat
     * @param upperBoundLat
     * @return
     */
    public static boolean[] getPositionInfoOfPointRelativeToCell(double pointToCheckLat, double pointToCheckLon,
                                                                 double leftBoundLon, double rightBoundLon,
                                                                 double lowerBoundLat, double upperBoundLat) {
        //todo: comment this **PLEASE**
        // if set to true, true, true, true: left, right, top, bottom
        boolean[] position = new boolean[4];

        if (leftBoundLon < -175) {
            if (pointToCheckLon > 50) {
                position[0] = true;
            } else if (pointToCheckLon < -175 && pointToCheckLon > -180 && pointToCheckLon < leftBoundLon) {
                position[0] = true;
            }
        } else if (rightBoundLon > 175) {
            if (pointToCheckLon < -50) {
                position[1] = true;
            } else if (pointToCheckLon > 175 && pointToCheckLon < 180 && pointToCheckLon > rightBoundLon) {
                position[1] = true;
            }
        } else if (pointToCheckLon < leftBoundLon) {
            position[0] = true;
        } else if (pointToCheckLon > rightBoundLon) {
            position[1] = true;
        }

        if (pointToCheckLat < lowerBoundLat) {
            position[3] = true;
        } else if (pointToCheckLat > upperBoundLat) {
            position[2] = true;
        }

        return position;
    }


    public static double[] latLonToVector(double lat, double lon) {
        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon);

        double latCos = Math.cos(lat);

        double[] vector = {
                latCos * Math.cos(lon),
                latCos * Math.sin(lon),
                Math.sin(lat)
        };

        return vector;
    }

    public static double[] addVectors(double a[], double b[]) {
        double[] sum = {
                a[0] + b[0],
                a[1] + b[1],
                a[2] + b[2]
        };

        return sum;
    }

    public static double[] crossProductOfVector(double a[], double u[]) {
        double[] cross = {
                a[1] * u[2] - a[2] * u[1],
                a[2] * u[0] - a[0] * u[2],
                a[0] * u[1] - a[1] * u[0]
        };

        return cross;
    }

    public static double dotProductOfVector(double a[], double b[]) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }


    /**
     * Implementation from javascript section of: http://www.movable-type.co.uk/scripts/latlong-vectors.html
     *
     * @param arcAStartPointLat
     * @param arcAStartPointLon
     * @param arcAEndPointLat
     * @param arcAEndPointLat
     * @return
     */
    public static boolean arcsIntersect(double arcAStartPointLat, double arcAStartPointLon,
                                 double arcAEndPointLat, double arcAEndPointLon,
                                 double arcBStartPointLat, double arcBStartPointLon,
                                 double arcBEndPointLat, double arcBEndPointLon) {
        // Transform coordinates to vectors
        double[] arcAStartVector = latLonToVector(arcAStartPointLat, arcAStartPointLon);
        double[] arcAEndVector = latLonToVector(arcAEndPointLat, arcAEndPointLon);

        double[] arcBStartVector = latLonToVector(arcBStartPointLat, arcBStartPointLon);
        double[] arcBEndVector = latLonToVector(arcBEndPointLat, arcBEndPointLon);

        double[] c1 = crossProductOfVector(arcAStartVector, arcAEndVector);
        double[] c2 = crossProductOfVector(arcBStartVector, arcBEndVector);

        double[] i1 = crossProductOfVector(c1, c2);

        double[] midPoint = addVectors(arcAStartVector, addVectors(arcAEndVector, addVectors(arcBStartVector, arcBEndVector)));

        double dot = dotProductOfVector(midPoint, i1);
        if (dot > 0.0001 || dot < -0.0001) {
            return true;
        } else {
            return false;
        }
    }

    public static double degreeCoordinateToRadian(double degreeCoord) {
        return degreeCoord * Math.PI / 180;
    }

    public static double radianCoordToDegree(double radianCoord) {
        return radianCoord *  180 / Math.PI;
    }


    /**
     * Checks if an arc defined by start point A (pointALat, pointALon) and
     * destination point B (pointBLat, pointBLon) intersects with a given longitude
     * latitudeToCheck.
     * <p>
     * Source: https://edwilliams.org/avform147.htm#Par
     *
     * @param pointALat         |
     * @param pointALon         |
     * @param pointBLat         |  x--------------------x
     * @param pointBLon         |
     * @param latToIntersect
     * @return
     */
    public static boolean crossesLatitude(double pointALat, double pointALon, double pointBLat,
                                   double pointBLon, double latToIntersect, double lonValStart, double lonValDest) {
        pointALat = degreeCoordinateToRadian(pointALat);
        pointBLat = degreeCoordinateToRadian(pointBLat);
        pointALon = degreeCoordinateToRadian(pointALon);
        pointBLon = degreeCoordinateToRadian(pointBLon);
        latToIntersect = degreeCoordinateToRadian(latToIntersect);
        lonValStart = degreeCoordinateToRadian(lonValStart);
        lonValDest = degreeCoordinateToRadian(lonValDest);

        double lonDiff = pointALon - pointBLon;
        double A = Math.sin(pointALat) * Math.cos(pointBLat) * Math.cos(latToIntersect) * Math.sin(lonDiff);
        double B = Math.sin(pointALat) * Math.cos(pointBLat) * Math.cos(latToIntersect) * Math.cos(lonDiff) - Math.cos(pointALat) * Math.sin(pointBLat) * Math.cos(latToIntersect);
        double C = Math.cos(pointALat) * Math.cos(pointBLat) * Math.sin(latToIntersect) * Math.sin(lonDiff);
        double lon = Math.atan2(B, A);

        double sumABsquared = Math.sqrt(A*A + B*B);

        if (Math.abs(C) > sumABsquared) {
            // no crossing
            return false;
        } else {
            double dlon = Math.acos(C/sumABsquared);
            // Intersection longitudes
            double crossLonA = mod(pointALon + dlon + lon + Math.PI, 2*Math.PI) - Math.PI;
            double crossLonB = mod(pointALon - dlon + lon + Math.PI, 2*Math.PI) - Math.PI;

            double maxLon = Math.max(pointALon, pointBLon);
            double minLon = Math.min(pointALon, pointBLon);
            double lonMax = Math.max(lonValStart, lonValDest);
            double lonMin = Math.min(lonValStart, lonValDest);
            boolean firstIntersectValid = pointLonBetweenLongitudes(crossLonA, minLon, maxLon) &&
                    pointLonBetweenLongitudes(crossLonA, lonMin, lonMax);
            boolean secondIntersectValid = pointLonBetweenLongitudes(crossLonB, minLon, maxLon) &&
                    pointLonBetweenLongitudes(crossLonB, lonMin, lonMax);
            return firstIntersectValid || secondIntersectValid;
        }
    }

    private static boolean pointLonBetweenLongitudes(double lonToCheck, double minLon, double maxLon) {
        boolean retVal = false;
        if(maxLon > 175.0 && minLon < -175) { //special case with wraparound is to be considered
            retVal = (lonToCheck < 180.0 && lonToCheck > maxLon) || (lonToCheck > -180.0 && lonToCheck < minLon);
        } else {
            retVal = (lonToCheck < maxLon && lonToCheck > minLon);
        }
        return retVal;
    }

    /**
     * Source: https://edwilliams.org/avform147.htm#Math
     *
     * @param y
     * @param x
     * @return
     */
    public static double mod(double y, double x) {
        double mod = y - x * Math.floor(y/x);
        return mod;
    }

    //earths radius is required for distance calculation
    private static final double EARTH_RADIUS = 6371000.0;

    /**
     * Calculates the distance between two points basted on latitude and longitude.
     * Formulas used can be found here: http://www.movable-type.co.uk/scripts/latlong.html
     *
     * @param startLat  latitude of the first point
     * @param startLong longitude of the first point
     * @param endLat    latitude of the second point
     * @param endLong   longitude of the second point
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
     *
     * @param coordinate
     * @return
     */
    private static double convertToRadian(double coordinate) {
        return coordinate * (Math.PI / 180.0);
    }
}
