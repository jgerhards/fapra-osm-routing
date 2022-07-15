package de.fmi.searouter.coastlinegrid;

import de.fmi.searouter.utils.IntersectionHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class CoastlineChecker implements Serializable {

    private static final long serialVersionUID = 13424412415L;

    private static CoastlineChecker INSTANCE;
    private static GridCell[][] topLevelGrid;

    public static CoastlineChecker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CoastlineChecker();
        }

        return INSTANCE;
    }

    // private constructor, this is a singleton!
    private CoastlineChecker() {
        topLevelGrid = new GridCell[18][36];
        for(int latIdx = 0; latIdx < 18; latIdx++) {
            double lowerLatBound = 10.0 * (latIdx - 9);
            double upperLatBound = lowerLatBound + 10.0;
            for(int lonIdx = 0; lonIdx < 36; lonIdx++) {
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

                topLevelGrid[latIdx][lonIdx] = new GridParent(edgesInCell, lowerLatBound, upperLatBound,
                        leftLonBound, rightLonBound);

            }
        }
    }

    public boolean pointInWater(float lat, float lon) {
        // Calculate the grid cell indices for the cell array depending on lat/lon
        int latIdx = (int) ((lat + 90) - (lat % 10) / 10);
        int lonIdx = (int) ((lon + 180) - (lon % 10) / 10);

        return topLevelGrid[latIdx][lonIdx].isPointInWater(lat, lon);
    }

}
