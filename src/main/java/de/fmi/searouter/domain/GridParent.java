package de.fmi.searouter.domain;

import de.fmi.searouter.utils.IntersectionHelper;

import java.util.ArrayList;
import java.util.List;

// TODO himmelsrichtung statt left, right etc
public class GridParent extends GridCell {

    private double lowerLatitude;
    private double upperLatitude;
    private double leftLongitude;
    private double rightLongitude;

    private GridCell[][] lowerLevelCells;


    public GridParent(List<Integer> edgeIDs, double lowerLatitude, double upperLatitude, double leftLongitude, double rightLongitude) {
        this.lowerLatitude = lowerLatitude;
        this.upperLatitude = upperLatitude;
        this.leftLongitude = leftLongitude;
        this.rightLongitude = rightLongitude;

        lowerLevelCells = new GridCell[3][3];

        buildLowerLevel(edgeIDs);
    }

    private void buildLowerLevel(List<Integer> edgeIDs) {

        double latSeparation = (upperLatitude - lowerLatitude) / 3;
        double lonSeparation = (leftLongitude - rightLongitude) / 3;

        double[] lowerLevelLat = new double[]{
                lowerLatitude, lowerLatitude + latSeparation, lowerLatitude + 2 * latSeparation, upperLatitude
        };


        double[] lowerLevelLon = new double[]{
                leftLongitude, leftLongitude + lonSeparation, leftLongitude + 2 * lonSeparation, rightLongitude
        };

        // assign edges to sub grids
        for (int latIdx = 0; latIdx < 3; latIdx++) {

            for (int lonIdx = 0; lonIdx < 3; lonIdx++) {

                List<Integer> edgesInCell = new ArrayList<>();
                for (Integer edgeId : edgeIDs) {
                    boolean[] startResults = IntersectionHelper.getPositionInfoOfPointRelativeToCell(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            lowerLevelLon[lonIdx], lowerLevelLon[lonIdx + 1],
                            lowerLevelLat[latIdx], lowerLevelLat[latIdx + 1]);
                    if (!(startResults[0] || startResults[1] || startResults[2] || startResults[3])) { // is inside
                        edgesInCell.add(edgeId);
                        continue;
                    }
                    boolean[] destResults = IntersectionHelper.getPositionInfoOfPointRelativeToCell(
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLevelLon[lonIdx], lowerLevelLon[lonIdx + 1],
                            lowerLevelLat[latIdx], lowerLevelLat[latIdx + 1]);
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
                            lowerLevelLat[latIdx], lowerLevelLon[lonIdx],
                            lowerLevelLat[latIdx + 1], lowerLevelLon[lonIdx] // Left vertical edge of cell, e.g. 0,0 - 1,0
                            )) {
                        intersectsBorder = true;
                    } else if (IntersectionHelper.arcsIntersect(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLevelLat[latIdx], lowerLevelLon[lonIdx+1],
                            lowerLevelLat[latIdx+1], lowerLevelLon[lonIdx+1] // Right vertical edge of cell, e.g. 0,1 - 1,1
                    )) {
                        intersectsBorder = true;
                    } else if (IntersectionHelper.crossesLatitude(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLevelLat[latIdx] // lower latitude coord of cell
                    )) {
                        intersectsBorder = true;
                    } else if (IntersectionHelper.crossesLatitude(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLevelLat[latIdx + 1] // upper latitude coord of cell
                    )) {
                        intersectsBorder = true;
                    }

                    if (intersectsBorder) {
                        edgesInCell.add(edgeId);
                    }
                }

            }

        }


        // for every sub grid: how many coastline edges?

        //

    }

    @Override
    public void initializeCellBorders() {

    }

    @Override
    public void initCenterPoint() {

    }

    @Override
    public boolean isPointInWater(float lat, float lon) {
        return false;
    }

    @Override
    public List<Integer> getContainedEdgesIDs() {
        return null;
    }
}
