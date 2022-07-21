package de.fmi.searouter.coastlinegrid;

import de.fmi.searouter.dijkstragrid.GridNode;
import de.fmi.searouter.utils.IntersectionHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class CoastlineChecker implements Serializable {
    private static final double INITIAL_POINT_LAT = -83.0;
    private static final double INITIAL_POINT_LON = -170.0;
    private static final boolean INITIAL_POINT_IN_WATER = false;

    private static final long serialVersionUID = 13424412415L;

    private static CoastlineChecker INSTANCE;
    private static GridCell[][] topLevelGrid;

    public static CoastlineChecker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CoastlineChecker();
        }

        return INSTANCE;
    }

    public List<GridNode> getAllCenterPoints(int maxDepth) {

        int currDepth = 0;

        List<GridNode> resultList = new ArrayList<>();

        for(int latIdx = 0; latIdx < 18; latIdx++) {
            for(int lonIdx = 0; lonIdx < 36; lonIdx++) {
                topLevelGrid[latIdx][lonIdx].getAllCenterPoints(currDepth + 1, maxDepth, resultList);
                /*if (currDepth + 1 <= maxDepth) {
                } else {
                    return resultList;
                }*/

            }
        }


        return resultList;

    }

    // private constructor, this is a singleton!
    private CoastlineChecker() {
        topLevelGrid = new GridCell[18][36];

        for(int latIdx = 0; latIdx < 18; latIdx++) {
            double lowerLatBound = 10.0 * (latIdx - 9);
            double upperLatBound = lowerLatBound + 10.0;
            for(int lonIdx = 0; lonIdx < 36; lonIdx++) {
                if(latIdx == 0 && lonIdx == 0) {
                    int a = 5; //todo: remove
                }
                double leftLonBound = 10.0 * (lonIdx - 18);
                double rightLonBound = leftLonBound + 10.0;

                List<Integer> edgesInCell = new ArrayList();
                int numOfEdges = CoastlineWays.getNumberOfEdges();
                for (int edgeId = 0; edgeId < numOfEdges; edgeId++) {
                    boolean[] startResults = IntersectionHelper.getPositionInfoOfPointRelativeToCellRough(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            leftLonBound, rightLonBound,
                            lowerLatBound, upperLatBound);
                    if (!(startResults[0] || startResults[1] || startResults[2] || startResults[3])) { // is inside
                        edgesInCell.add(edgeId);
                        continue;
                    }
                    boolean[] destResults = IntersectionHelper.getPositionInfoOfPointRelativeToCellRough(
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            leftLonBound, rightLonBound,
                            lowerLatBound, upperLatBound);
                    if (!(destResults[0] || destResults[1] || destResults[2] || destResults[3])) { // is inside
                        edgesInCell.add(edgeId);
                        continue;
                    }

                    //check if intersection test is necessary or edge is trivially not in cell
                    if ((startResults[0] && destResults[0]) || (startResults[1] && destResults[1]) ||
                            (startResults[2] && destResults[2]) || (startResults[3] && destResults[3])) {
                        //trivially not contained
                        continue;
                    }

                    boolean intersectsBorder = false;
                    if (IntersectionHelper.arcsIntersect(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLatBound, leftLonBound,
                            upperLatBound, leftLonBound // Left vertical edge of cell, e.g. 0,0 - 1,0
                    )) {
                        intersectsBorder = true;
                    } else if (IntersectionHelper.arcsIntersect(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLatBound, rightLonBound,
                            upperLatBound, rightLonBound // Right vertical edge of cell, e.g. 0,1 - 1,1
                    )) {
                        intersectsBorder = true;
                    } else if (IntersectionHelper.crossesLatitude(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLatBound, // lower latitude coord of cell
                            leftLonBound, rightLonBound
                    )) {
                        intersectsBorder = true;
                    } else if (IntersectionHelper.crossesLatitude(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            upperLatBound, // upper latitude coord of cell
                            leftLonBound, rightLonBound
                    )) {
                        intersectsBorder = true;
                    }

                    if (intersectsBorder) {
                        edgesInCell.add(edgeId);
                    }
                }

                //todo: remove below
                if(latIdx == 0 && lonIdx == 0) {
                    for(Integer i : edgesInCell) {
                        System.out.println("ttt: " + CoastlineWays.getStartLatByEdgeIdx(i) + ", " +
                                CoastlineWays.getStartLonByEdgeIdx(i) + ", " + CoastlineWays.getDestLatByEdgeIdx(i) + ", "
                                + CoastlineWays.getDestLonByEdgeIdx(i) + ", " + i);
                    }
                }

                topLevelGrid[latIdx][lonIdx] = new GridParent(edgesInCell, lowerLatBound, upperLatBound,
                        leftLonBound, rightLonBound);

            }
        }

        //initialize middle points
        double[] centerPointLat = new double[18];
        double[] centerPointLon = new double[36];
        double lat = -85.0;
        for(int i = 0; i < 18; i++) {
            centerPointLat[i] = lat;
            lat += 10.0;
        }
        double lon = -175.0;
        for(int i = 0; i < 36; i++) {
            centerPointLon[i] = lon;
            lon += 10.0;
        }

        //first, check if the middle point of the first cell is in water
        boolean firstPointInWater = INITIAL_POINT_IN_WATER;
        int numOfEdges = CoastlineWays.getNumberOfEdges();
        System.out.print("ttt: init point: ");
        System.out.println(INITIAL_POINT_LAT + ", " +
                INITIAL_POINT_LON + ", " + centerPointLat[0] + ", "
                + centerPointLon[0]);
        for (int edgeId = 0; edgeId < numOfEdges; edgeId++) {
            if(edgeId==672380) {
                int a = 5;
            }
            if(IntersectionHelper.arcsIntersect(
                    CoastlineWays.getStartLatByEdgeIdx(edgeId),
                    CoastlineWays.getStartLonByEdgeIdx(edgeId),
                    CoastlineWays.getDestLatByEdgeIdx(edgeId),
                    CoastlineWays.getDestLonByEdgeIdx(edgeId),
                    INITIAL_POINT_LAT, INITIAL_POINT_LON,
                    centerPointLat[0], centerPointLon[0]
            )) {
                System.out.print("ttt: arcs intersect: ");
                System.out.println(CoastlineWays.getStartLatByEdgeIdx(edgeId) + ", " +
                        CoastlineWays.getStartLonByEdgeIdx(edgeId) + ", " + CoastlineWays.getDestLatByEdgeIdx(edgeId) + ", "
                        + CoastlineWays.getDestLonByEdgeIdx(edgeId) + ", " + edgeId);
                firstPointInWater = !firstPointInWater;
            }
        }

        System.out.println("ttt: firstPointInWater: " + firstPointInWater);
        //just this once, we use set instead of init (since point in water is known)
        topLevelGrid[0][0].setCenterPoint(centerPointLat[0], centerPointLon[0], firstPointInWater);

        boolean[] firstCenterPointInWater = new boolean[36];
        List<Integer>[] firstAdditionalEdges = new List[36];
        firstCenterPointInWater[0] = firstPointInWater;
        firstAdditionalEdges[0] = topLevelGrid[0][0].getAllContainedEdgeIDs();

        // first, calculate bottom row
        double firstRowLat = centerPointLat[0];
        for(int lonIdx = 1; lonIdx < 36; lonIdx++) {
            firstCenterPointInWater[lonIdx] = topLevelGrid[0][lonIdx].initCenterPoint(firstRowLat,
                    centerPointLon[lonIdx - 1], firstCenterPointInWater[lonIdx - 1],
                    firstAdditionalEdges[lonIdx - 1], GridCell.ApproachDirection.FROM_HORIZONTAL);
            firstAdditionalEdges[lonIdx] = topLevelGrid[0][lonIdx].getAllContainedEdgeIDs();
        }

        //now, calculate by column
        for(int lonIdx = 0; lonIdx < 36; lonIdx++) {
            boolean previousPointInWater = firstCenterPointInWater[lonIdx];
            List<Integer> previousEdges = firstAdditionalEdges[lonIdx];
            for(int latIdx = 1; latIdx < 18; latIdx++) { //first row already calculated, so start at idx 1
                previousPointInWater = topLevelGrid[latIdx][lonIdx].initCenterPoint(centerPointLat[latIdx - 1],
                        centerPointLon[lonIdx], previousPointInWater,
                        previousEdges, GridCell.ApproachDirection.FROM_VERTICAL);
                previousEdges = topLevelGrid[latIdx][lonIdx].getAllContainedEdgeIDs();
            }
        }

    }

    public boolean pointInWater(float lat, float lon) {
        int latIdx;
        int lonIdx;
        // Calculate the grid cell indices for the cell array depending on lat/lon
        if(lat == 90) {
            latIdx = 17;
        } else {
            latIdx = (int) (((lat + 90) - (lat % 10)) / 10);
        }

        if(lon == 180) {
            lonIdx = 35;
        } else {
            lonIdx = (int) (((lon + 180) - (lon % 10)) / 10);
        }

        return topLevelGrid[latIdx][lonIdx].isPointInWater(lat, lon);
    }

}
