package de.fmi.searouter.coastlinegrid;

import de.fmi.searouter.importdata.CoastlineWay;
import de.fmi.searouter.importdata.Point;

import java.util.List;

public class CoastlineWays {

    // TODO evtl bei double

    private static int[] edgePosStart;

    private static float[] pointLon;
    private static float[] pointLat;

    public static void initEdges(List<CoastlineWay> importedCoastlines) {


        int numberOfEdges = 0;
        int numberOfCoordinates = 0;
        for (CoastlineWay currCoastline : importedCoastlines) {
            numberOfEdges += currCoastline.getNumberOfEdges();
            numberOfCoordinates += currCoastline.getNumberOfPoints();
        }

        edgePosStart = new int[numberOfEdges];

        pointLon = new float[numberOfCoordinates];
        pointLat = new float[numberOfCoordinates];

        int nextEdgeIdx = 0;
        int nextCoordIdx = 0;

        for (CoastlineWay currCoastlineWay : importedCoastlines) {

            List<Point> currPoints = currCoastlineWay.getPoints();
            if(currPoints.get(0).getId() == 2413800368L) {
                int breakpoint = 1;
            }

            if (currPoints.size() <= 1) {
                continue;
            }

            int currPointsSize = currPoints.size();

            for (int pointIdx = 0; pointIdx < currPointsSize; pointIdx++) {
                Point currPoint = currPoints.get(pointIdx);

                if (pointIdx == currPointsSize - 1) {

                    pointLon[nextCoordIdx] = currPoint.getLon();
                    pointLat[nextCoordIdx] = currPoint.getLat();

                    nextCoordIdx++;

                } else {

                    pointLon[nextCoordIdx] = currPoint.getLon();
                    pointLat[nextCoordIdx] = currPoint.getLat();

                    edgePosStart[nextEdgeIdx] = nextCoordIdx;

                    nextCoordIdx++;
                    nextEdgeIdx++;
                }

            }
        }
    }

    public static float getStartLatByEdgeIdx(int edgeIdx) {
        return pointLat[edgePosStart[edgeIdx]];
    }

    public static float getDestLatByEdgeIdx(int edgeIdx) {
        return pointLat[edgePosStart[edgeIdx]+1];
    }

    public static float getStartLonByEdgeIdx(int edgeIdx) {
        return pointLon[edgePosStart[edgeIdx]];
    }

    public static float getDestLonByEdgeIdx(int edgeIdx) {
        return pointLon[edgePosStart[edgeIdx]+1];
    }

    public static int getNumberOfEdges() {
        return edgePosStart.length;
    }
}
