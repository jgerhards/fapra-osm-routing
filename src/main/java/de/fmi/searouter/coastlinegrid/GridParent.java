package de.fmi.searouter.coastlinegrid;

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

    private double[] innerLatBorders;
    private double [] innerLonBorders;



    public GridParent(List<Integer> edgeIDs, double lowerLatitude, double upperLatitude, double leftLongitude,
                      double rightLongitude) {
        this.lowerLatitude = lowerLatitude;
        this.upperLatitude = upperLatitude;
        this.leftLongitude = leftLongitude;
        this.rightLongitude = rightLongitude;

        lowerLevelCells = new GridCell[3][3];

        // inner border arrays initialized in this function
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

        //assign inner border arrays, since we will need these values after construction of this object
        innerLatBorders = new double[]{lowerLevelLat[1], lowerLevelLat[2]};
        innerLonBorders = new double[]{lowerLevelLon[1], lowerLevelLon[2]};

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

                    // For cases where corner cases still exists (not trivial) a check of intersection is needed
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
                            lowerLevelLat[latIdx], // lower latitude coord of cell
                            lowerLevelLon[latIdx], lowerLevelLon[latIdx+1]
                    )) {
                        intersectsBorder = true;
                    } else if (IntersectionHelper.crossesLatitude(
                            CoastlineWays.getStartLatByEdgeIdx(edgeId),
                            CoastlineWays.getStartLonByEdgeIdx(edgeId),
                            CoastlineWays.getDestLatByEdgeIdx(edgeId),
                            CoastlineWays.getDestLonByEdgeIdx(edgeId),
                            lowerLevelLat[latIdx + 1], // upper latitude coord of cell
                            lowerLevelLon[latIdx], lowerLevelLon[latIdx+1]
                    )) {
                        intersectsBorder = true;
                    }

                    if (intersectsBorder) {
                        edgesInCell.add(edgeId);
                    }
                }

                if(edgesInCell.size() >= EDGE_THRESHOLD) {
                    // A new grid cell subdivision is needed, due to the threshold being exceeded
                    lowerLevelCells[latIdx][lonIdx] = new GridParent(edgesInCell, lowerLevelLat[latIdx],
                            lowerLevelLat[latIdx+1], lowerLevelLon[latIdx], lowerLevelLon[latIdx+1]);
                } else {
                    // Calculate center point of new cell
                    double ctrLat = (lowerLevelLat[latIdx] + lowerLevelLat[latIdx + 1]) / 2;
                    double ctrLon = (lowerLevelLon[lonIdx] + lowerLevelLon[lonIdx + 1]) / 2;

                    // Transform the edge index list to an edge index array
                    int listSize = edgesInCell.size();
                    int[] idArray = new int[listSize];
                    for(int i = 0; i < listSize; i++) {
                        idArray[i] = edgesInCell.get(i);
                    }

                    // As the number of edges is smaller than the threshold no further lower levels are needed --> leaf
                    lowerLevelCells[latIdx][lonIdx] = new GridLeaf(idArray, ctrLat, ctrLon);

                }

            }

        }

    }

    public void startCenterPointInitialization(boolean pointOnWater) {

    }

    @Override
    public void setCenterPoint(double lat, double lon, boolean isInWater) {
        //set middle center point of lower level
        lowerLevelCells[1][1].setCenterPoint(lat, lon, isInWater);
        List<Integer>[] middleEdgeLists = new List[] {
                lowerLevelCells[1][0].getAllContainedEdgeIDs(),
                lowerLevelCells[1][1].getAllContainedEdgeIDs(),
                lowerLevelCells[1][2].getAllContainedEdgeIDs()
        };

        boolean[] centersInWater = new boolean[3];
        centersInWater[1] = isInWater;
        centersInWater[0] = lowerLevelCells[1][0].initCenterPoint(lat, lon, isInWater, middleEdgeLists[1],
                ApproachDirection.FROM_HORIZONTAL);
        centersInWater[2] = lowerLevelCells[1][2].initCenterPoint(lat, lon, isInWater, middleEdgeLists[1],
                ApproachDirection.FROM_HORIZONTAL);

        for(int i = 0; i < 3; i++) {
            lowerLevelCells[0][i].initCenterPoint(lat, lon, centersInWater[i], middleEdgeLists[i],
                    ApproachDirection.FROM_VERTICAL);
            lowerLevelCells[2][i].initCenterPoint(lat, lon, centersInWater[i], middleEdgeLists[i],
                    ApproachDirection.FROM_VERTICAL);
        }
    }

    @Override
    public boolean initCenterPoint(double originCenterPointLat, double originCenterPointLon,
                                boolean originCenterPointInWater, List<Integer> additionalEdges,
                                ApproachDirection dir) {
        boolean centerInWater = false;
        double centerLat = (lowerLatitude + upperLatitude) / 2;
        double centerLon = (leftLongitude + rightLongitude) / 2;
        if(dir == ApproachDirection.FROM_HORIZONTAL) {
            centerInWater = originCenterPointInWater;
            for (Integer edgeId : additionalEdges) {
                if (IntersectionHelper.crossesLatitude(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), centerLat, originCenterPointLon, centerLon)) {
                    centerInWater = !centerInWater;
                }
            }

            //check for edges from the left and middle subnodes
            for (Integer edgeId : lowerLevelCells[1][1].getAllContainedEdgeIDs()) {
                if (IntersectionHelper.crossesLatitude(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), centerLat, originCenterPointLon, centerLon)) {
                    centerInWater = !centerInWater;
                }
            }
            for (Integer edgeId : lowerLevelCells[1][0].getAllContainedEdgeIDs()) {
                if (IntersectionHelper.crossesLatitude(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), centerLat, originCenterPointLon, centerLon)) {
                    centerInWater = !centerInWater;
                }
            }
        } else {  //approach from top
            centerInWater = originCenterPointInWater;
            for (Integer edgeId : additionalEdges) {
                if (IntersectionHelper.arcsIntersect(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), centerLat, centerLon,
                        originCenterPointLat, originCenterPointLon)) {
                    centerInWater = !centerInWater;
                }
            }

            //check for edges from the left and middle subnodes
            for (Integer edgeId : lowerLevelCells[1][1].getAllContainedEdgeIDs()) {
                if (IntersectionHelper.arcsIntersect(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), centerLat, centerLon,
                        originCenterPointLat, originCenterPointLon)) {
                    centerInWater = !centerInWater;
                }
            }
            for (Integer edgeId : lowerLevelCells[1][0].getAllContainedEdgeIDs()) {
                if (IntersectionHelper.arcsIntersect(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), centerLat, centerLon,
                        originCenterPointLat, originCenterPointLon)) {
                    centerInWater = !centerInWater;
                }
            }
        }

        setCenterPoint(centerLat, centerLon, centerInWater);

        return centerInWater;
    }

    @Override
    public boolean isPointInWater(float lat, float lon) {
        //first, determine lat idx of responsible lower level cell
        int latIdx;
        if(lat < innerLatBorders[0]) {
            latIdx = 0;
        } else if(lat < innerLatBorders[1]) {
            latIdx = 1;
        } else {
            latIdx = 2;
        }

        //then, determine lon idx of responsible lower level cell
        int lonIdx;
        if(lon < innerLonBorders[0]) {
            lonIdx = 0;
        } else if(lon < innerLonBorders[1]) {
            lonIdx = 1;
        } else {
            lonIdx = 2;
        }

        //pass call to responsible cell
        return lowerLevelCells[latIdx][lonIdx].isPointInWater(lat, lon);
    }

    @Override
    public List<Integer> getAllContainedEdgeIDs() {
        List<Integer> fullList = new ArrayList();
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                fullList.addAll(lowerLevelCells[i][j].getAllContainedEdgeIDs());
            }
        }
        return fullList;
    }
}
