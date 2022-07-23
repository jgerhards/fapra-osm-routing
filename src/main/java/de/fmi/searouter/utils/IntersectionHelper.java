package de.fmi.searouter.utils;

import com.google.common.math.DoubleMath;

import java.util.Arrays;

/**
 * Geometrical spheric calculation helper methods.
 */
public class IntersectionHelper {

    public static final int EARTH_RADIUS_METERS = 6371 * 1000;

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

        /*if (leftBoundLon < -175) {
            if (pointToCheckLon > 50) {
                System.out.println("ttt: case 1");
                position[0] = true;
            } else if (pointToCheckLon < -175 && pointToCheckLon >= -180 && pointToCheckLon < leftBoundLon) {
                System.out.println("ttt: case 2");
                position[0] = true;
            }
        } else if (rightBoundLon > 175) {
            if (pointToCheckLon < -50) {
                System.out.println("ttt: case 3");
                position[1] = true;
            } else if (pointToCheckLon > 175 && pointToCheckLon <= 180 && pointToCheckLon > rightBoundLon) {
                System.out.println("ttt: case 4");
                position[1] = true;
            }
        } else*/ if (pointToCheckLon < leftBoundLon) {
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

    public static double length(double v[]) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    public static double distance(double v[], double u[]) {
        return EARTH_RADIUS_METERS * Math.atan2(length(crossProductOfVector(v, u)), dotProductOfVector(v, u));
    }

    public static boolean arcsIntersectWithException(
            double latSourceA, double lonSourceA,
            double latDestA, double lonDestA,
            double latSourceB, double lonSourceB,
            double latDestB, double lonDestB) throws IllegalArgumentException {
        /*if(DoubleMath.fuzzyEquals(lonSourceA, lonDestA, 0.0000001) &&
                DoubleMath.fuzzyEquals(lonDestA, lonSourceB, 0.0000001) &&
                DoubleMath.fuzzyEquals(lonSourceB, lonDestB, 0.0000001)) {*/
        if(DoubleMath.fuzzyEquals(lonSourceA, lonDestB, 0.0000001) ||
                    DoubleMath.fuzzyEquals(lonDestA, lonDestB, 0.0000001)) {
            System.out.println("bbbbbbbbbbbb");
            throw new IllegalArgumentException();
        }
        return arcsIntersect(latSourceA, lonSourceA, latDestA, lonDestA, latSourceB, lonSourceB, latDestB, lonDestB);
    }

    public static boolean arcsIntersect(
            double latSourceA, double lonSourceA,
            double latDestA, double lonDestA,
            double latSourceB, double lonSourceB,
            double latDestB, double lonDestB) {
        if(DoubleMath.fuzzyEquals(lonSourceA, lonDestA, 0.0000001) &&
                DoubleMath.fuzzyEquals(lonDestA, lonSourceB, 0.0000001) &&
                DoubleMath.fuzzyEquals(lonSourceB, lonDestB, 0.0000001)) {
            System.out.println("ggggggggggg");
            return true;
        }

        return segmentIntersection(
                latLonToVector(latSourceA, lonSourceA),
                latLonToVector(latDestA, lonDestA),
                latLonToVector(latSourceB, lonSourceB),
                latLonToVector(latDestB, lonDestB), new double[3]);
    }

    private static boolean segmentIntersection(double[] sourceA, double[] destA, double[] sourceB, double[] destB, double[] target) {
        boolean intersect = lineIntersection(sourceA, destA, sourceB, destB, target);
        if (isLineIntersectionInSegment(sourceA, destA, target) && isLineIntersectionInSegment(sourceB, destB, target)) {
            return intersect;
        } else {
            return false;
        }
    }

    private static boolean isLineIntersectionInSegment(double[] source, double[] dest, double[] inter) {
        double segmentDistance = distance(source, dest);
        return distance(inter, source) <= segmentDistance && distance(inter, dest) <= segmentDistance;
    }


    private static boolean lineIntersection(double[] sourceA, double[] destA, double[] sourceB, double[] destB, double[] target) {
        double[] c1 = crossProductOfVector(sourceA, destA);
        double[] c2 = crossProductOfVector(sourceB, destB);

        double[] i1 = crossProductOfVector(c1, c2);
        double[] i2 = crossProductOfVector(c2, c1);

        // const mid = p1.plus(p2).plus(path1brngEnd.toNvector()).plus(path2brngEnd.toNvector());
        double[] midPoint = addVectors(sourceA, addVectors(destA, addVectors(sourceB, destB)));

        double dot = dotProductOfVector(midPoint, i1);
        if (dot > 0) {
            System.arraycopy(i1, 0, target, 0, 3);
            return true;
        } else if (dot < 0) {
            System.arraycopy(i2, 0, target, 0, 3);
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

    public static boolean crossesLatitudeWithException(double pointALat, double pointALon, double pointBLat,
                                          double pointBLon, double latToIntersect, double lonValStart, double lonValDest)
    throws IllegalArgumentException{
        if(DoubleMath.fuzzyEquals(pointALat, latToIntersect, 0.0000001) ||
                DoubleMath.fuzzyEquals(pointBLat, latToIntersect, 0.0000001)) {
            System.out.println("ooooooooooooo");
            throw new IllegalArgumentException();
        }
        return crossesLatitude(pointALat, pointALon, pointBLat, pointBLon, latToIntersect, lonValStart, lonValDest);
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
        if(DoubleMath.fuzzyEquals(pointALat, latToIntersect, 0.0000001) ||
                DoubleMath.fuzzyEquals(pointBLat, latToIntersect, 0.0000001)) {
            return true;
        }

        pointALat = degreeCoordinateToRadian(pointALat);
        pointBLat = degreeCoordinateToRadian(pointBLat);
        pointALon = degreeCoordinateToRadian(pointALon);
        pointBLon = degreeCoordinateToRadian(pointBLon);
        latToIntersect = degreeCoordinateToRadian(latToIntersect);
        lonValStart = degreeCoordinateToRadian(lonValStart);
        lonValDest = degreeCoordinateToRadian(lonValDest);

        double latMax = Math.max(pointALat, pointBLat);
        double latMin = Math.min(pointALat, pointBLat);
        if(!pointLonBetweenLongitudes(latToIntersect, latMin, latMax)) {
            return false;
        }

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

        if(DoubleMath.fuzzyEquals(lonToCheck, minLon, 0.0000001) &&
                DoubleMath.fuzzyEquals(minLon, maxLon, 0.0000001)) {
            //System.out.println("ccc");
            return true;
        }

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
