package de.fmi.searouter.domain;

import java.util.Arrays;

public class IntersectionHelper {
    //earths radius is required for distance calculation
    private static final double EARTH_RADIUS = 6371000.0;
    private static final double THRESHOLD = 0.000000000001;

    /**
     * Checks if two arcs of a great circle intersect each other. Arcs are given using start and end point coordinates.
     * If arcs intersect twice, false is returned since this cancels out for the purposes we need this function for
     * (coastline checking).
     * Formulas used can be found here: http://www.boeing-727.com/Data/fly%20odds/distance.html
     * @param firstLineStartLat latitude of the start point of the first arc.
     * @param firstLineStartLong longitude of the start point of the first arc.
     * @param firstLineEndLat latitude of the end point of the first arc.
     * @param firstLineEndLong longitude of the end point of the first arc.
     * @param secondLineStartLat latitude of the start point of the second arc.
     * @param secondLineStartLong longitude of the start point of the second arc.
     * @param secondLineEndLat latitude of the end point of the second arc.
     * @param secondLineEndLong longitude of the end point of the second arc.
     * @return
     */
    public static boolean linesIntersect(double firstLineStartLat, double firstLineStartLong, double firstLineEndLat,
                                         double firstLineEndLong, double secondLineStartLat, double secondLineStartLong,
                                         double secondLineEndLat, double secondLineEndLong) {
        //System.out.println("ttt: lines intersect called");
        //check to make sure no line has´the same start and end point
        //todo: maybe use equals function instead of "==" ?
        if((firstLineStartLat == firstLineEndLat && firstLineStartLong == firstLineEndLong) ||
                (secondLineStartLat == secondLineEndLat && secondLineStartLong == secondLineEndLong)) {
            System.out.println("ttt: ret 1");
            return false;
        }

        //convert to radian representation
        double radianFirstStartLat = convertToRadian(firstLineStartLat);
        double radianFirstStartLong = convertToRadian(firstLineStartLong);
        double radianFirstEndLat = convertToRadian(firstLineEndLat);
        double radianFirstEndLong = convertToRadian(firstLineEndLong);
        //System.out.println("ttt: radian values 1: " + radianFirstStartLat + " " + radianFirstStartLong + " "
        //        + radianFirstEndLat + " " + radianFirstEndLong);

        double radianSecondStartLat = convertToRadian(secondLineStartLat);
        double radianSecondStartLong = convertToRadian(secondLineStartLong);
        double radianSecondEndLat = convertToRadian(secondLineEndLat);
        double radianSecondEndLong = convertToRadian(secondLineEndLong);
        //System.out.println("ttt: radian values 2: " + radianSecondStartLat + " " + radianSecondStartLong + " "
        //        + radianSecondEndLat + " " + radianSecondEndLong);

        //convert to cartesian representation
        //todo: maybe a separate function? Not sure if useful...
        double[] cartesianFirstStart = {Math.cos(radianFirstStartLat) * Math.cos(radianFirstStartLong),
                Math.cos(radianFirstStartLat) * Math.sin(radianFirstStartLong),
                Math.sin(radianFirstStartLat)};
        double[] cartesianFirstEnd = {Math.cos(radianFirstEndLat) * Math.cos(radianFirstEndLong),
                Math.cos(radianFirstEndLat) * Math.sin(radianFirstEndLong),
                Math.sin(radianFirstEndLat)};
        double[] cartesianSecondStart = {Math.cos(radianSecondStartLat) * Math.cos(radianSecondStartLong),
                Math.cos(radianSecondStartLat) * Math.sin(radianSecondStartLong),
                Math.sin(radianSecondStartLat)};
        double[] cartesianSecondEnd = {Math.cos(radianSecondEndLat) * Math.cos(radianSecondEndLong),
                Math.cos(radianSecondEndLat) * Math.sin(radianSecondEndLong),
                Math.sin(radianSecondEndLat)};
        //System.out.println("ttt: cartesian values: " + Arrays.toString(cartesianFirstStart) + " " +
        //        Arrays.toString(cartesianFirstEnd) + " "
        //        + Arrays.toString(cartesianSecondStart) + " " + Arrays.toString(cartesianSecondEnd));

        //calculate equation of the planes
        double[] firstPlane = {cartesianFirstStart[1]*cartesianFirstEnd[2] -
                cartesianFirstStart[2]*cartesianFirstEnd[1],
                cartesianFirstStart[2]*cartesianFirstEnd[0] -
                        cartesianFirstStart[0]*cartesianFirstEnd[2],
                cartesianFirstStart[0]*cartesianFirstEnd[1] -
                        cartesianFirstStart[1]*cartesianFirstEnd[0]};
        double[] secondPlane = {cartesianSecondStart[1]*cartesianSecondEnd[2] -
                cartesianSecondStart[2]*cartesianSecondEnd[1],
                cartesianSecondStart[2]*cartesianSecondEnd[0] -
                        cartesianSecondStart[0]*cartesianSecondEnd[2],
                cartesianSecondStart[0]*cartesianSecondEnd[1] -
                        cartesianSecondStart[1]*cartesianSecondEnd[0]};
        //System.out.println("ttt: plane values: " + Arrays.toString(firstPlane) + " " + Arrays.toString(secondPlane));

        //change to unit vector
        double firstLength = Math.sqrt(Math.pow(firstPlane[0], 2) + Math.pow(firstPlane[1], 2) +
                Math.pow(firstPlane[2], 2));
        for(int i = 0; i < firstPlane.length; i++) {
            firstPlane[i] = firstPlane[i] / firstLength;
        }
        double secondLength = Math.sqrt(Math.pow(secondPlane[0], 2) + Math.pow(secondPlane[1], 2) +
                Math.pow(secondPlane[2], 2));
        for(int i = 0; i < secondPlane.length; i++) {
            secondPlane[i] = secondPlane[i] / secondLength;
        }
        //System.out.println("ttt: normalized plane values: " + Arrays.toString(firstPlane) + " " + Arrays.toString(secondPlane));

        //check if the two planes are equal. If that is the case, they are not considered to intersect each other
        //todo: is this a good idea to proceed (not considering this an intersection)?
        boolean planesEqual = true;
        for(int i = 0; i < firstPlane.length; i++) {
            if(Math.abs(firstPlane[i] - secondPlane[i]) > THRESHOLD) {
                planesEqual = false;
                break;
            }
        }
        if(planesEqual) {
            System.out.println("ttt: ret 2");
            return false;
        }

        //calculate points of intersection (if planes are unequal, there are always two intersection points)
        double[] vectorDirector = {firstPlane[1]*secondPlane[2] - firstPlane[2]*secondPlane[1],
                firstPlane[2]*secondPlane[0] - firstPlane[0]*secondPlane[2],
                firstPlane[0]*secondPlane[1] - firstPlane[1]*secondPlane[0]};
        double directorLength = Math.sqrt(Math.pow(vectorDirector[0], 2) + Math.pow(vectorDirector[1], 2) +
                Math.pow(vectorDirector[2], 2));

        double[] firstIntersection = { vectorDirector[0] / directorLength, vectorDirector[1] / directorLength,
                vectorDirector[2] / directorLength};
        double[] secondIntersection = { -1*firstIntersection[0], -1*firstIntersection[1], -1*firstIntersection[2]};

        //calculate geographical location of intersections
        double firstIntersectLat = Math.asin(firstIntersection[2]);
        double temp = Math.cos(firstIntersectLat);
        double sign = Math.asin(firstIntersection[1] / temp);
        double firstIntersectLong = Math.acos(firstIntersection[0] / temp) * sign;

        System.out.println("ttt: first intersect " + firstIntersectLat + " " + firstIntersectLong);

        double secondIntersectLat = Math.asin(secondIntersection[2]);
        temp = Math.cos(secondIntersectLat);
        sign = Math.asin(secondIntersection[1] / temp);
        double secondIntersectLong = Math.acos(secondIntersection[0] / temp) * sign;

        System.out.println("ttt: second intersect " + secondIntersectLat + " " + secondIntersectLong);

        //use length to check if and if yes which of the points are on both lines
        double firstFullLength = getDistance(firstLineStartLat, firstLineStartLong, firstLineEndLat, firstLineEndLong);
        double firstPartialLength1 = getDistance(firstLineStartLat, firstLineStartLong,
                firstIntersectLat, firstIntersectLong);
        double firstPartialLength2 = getDistance(firstLineEndLat, firstLineEndLong,
                firstIntersectLat, firstIntersectLong);
        double secondFullLength = getDistance(secondLineStartLat, secondLineStartLong,
                secondLineEndLat, secondLineEndLong);
        double secondPartialLength1 = getDistance(secondLineStartLat, secondLineStartLong,
                firstIntersectLat, firstIntersectLong);
        double secondPartialLength2 = getDistance(secondLineEndLat, secondLineEndLong,
                firstIntersectLat, firstIntersectLong);
        boolean firstIntersectOnLines = (firstFullLength - firstPartialLength1 - firstPartialLength2 < THRESHOLD &&
                secondFullLength - secondPartialLength1 - secondPartialLength2 < THRESHOLD);

        //check for the second intersection
        firstPartialLength1 = getDistance(firstLineStartLat, firstLineStartLong,
                secondIntersectLat, secondIntersectLong);
        firstPartialLength2 = getDistance(firstLineEndLat, firstLineEndLong,
                secondIntersectLat, secondIntersectLong);
        secondPartialLength1 = getDistance(secondLineStartLat, secondLineStartLong,
                secondIntersectLat, secondIntersectLong);
        secondPartialLength2 = getDistance(secondLineEndLat, secondLineEndLong,
                secondIntersectLat, secondIntersectLong);
        boolean secondIntersectOnLines = (firstFullLength - firstPartialLength1 - firstPartialLength2 < THRESHOLD &&
                secondFullLength - secondPartialLength1 - secondPartialLength2 < THRESHOLD);

        //use XOR since if lines intersect twice it cancels out (and should not happen anyway)
        boolean linesIntersect = firstIntersectOnLines ^ secondIntersectOnLines;
        System.out.println("ttt: ret 3 " + linesIntersect + " first bool: " + firstIntersectOnLines + " 2nd " + secondIntersectOnLines);
        return linesIntersect;
    }

    public static boolean linesIntersectWithCoast(double firstLineStartLat, double firstLineStartLong,
                                                  double firstLineEndLat, double firstLineEndLong, int coastlineID) {
        //todo: second intersection checks which uses information from Coastlines class
        return false;
    }

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
     * Checks if a given point is loacted on a given line.
     * @param pointLat the latitude of the point
     * @param pointLong the longitude of the point
     * @param lineStartLat the latitude of the start point of the line
     * @param lineStartLong the longitude of the start point of the line
     * @param lineEndLat the latitude of the end point of the line
     * @param lineEndLong the longitude of the end point of the line
     * @return true if the given point is located on the line, else false
     */
    public static boolean pointOnLine(double pointLat, double pointLong, double lineStartLat, double lineStartLong,
                                      double lineEndLat, double lineEndLong) {
        double lineLength = getDistance(lineStartLat, lineStartLong, lineEndLat, lineEndLong);
        double firstPartialLen = getDistance(lineStartLat, lineStartLong, pointLat, pointLong);
        double secondPartialLen = getDistance(pointLat, pointLong, lineEndLat, lineEndLong);

        return (Math.abs((lineLength) - (firstPartialLen + secondPartialLen)) <= THRESHOLD);
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
