package de.fmi.searouter.coastlinegrid;

import de.fmi.searouter.dijkstragrid.GridNode;
import de.fmi.searouter.importdata.Point;
import de.fmi.searouter.utils.IntersectionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GridLeaf extends GridCell {

    private int[] edgeIds;
    private int edgeCount;

    private boolean centerPointInWater;

    private double latCenterPoint;
    private double lonCenterPoint;

    public GridLeaf(int[] edgeIds, double latCenterPoint, double lonCenterPoint) {
        this.edgeIds = edgeIds;
        this.edgeCount = edgeIds.length;
        this.latCenterPoint = latCenterPoint;
        this.lonCenterPoint = lonCenterPoint;
    }

    @Override
    public void setCenterPoint(double lat, double lon, boolean isInWater) {
        this.latCenterPoint = lat;
        this.lonCenterPoint = lon;
        this.centerPointInWater = isInWater;
    }

    @Override
    public boolean initCenterPoint(double originCenterPointLat, double originCenterPointLon,
                                boolean originCenterPointInWater, List<Integer> additionalEdges,
                                ApproachDirection dir) {
        centerPointInWater = originCenterPointInWater;
        if(dir == ApproachDirection.FROM_HORIZONTAL) {
            for (Integer edgeId : additionalEdges) {
                if (IntersectionHelper.crossesLatitude(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), latCenterPoint,
                        originCenterPointLon, lonCenterPoint)) {
                    centerPointInWater = !centerPointInWater;
                }
            }
            for (int edgeId : edgeIds) {
                if (IntersectionHelper.crossesLatitude(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), latCenterPoint,
                        originCenterPointLon, lonCenterPoint)) {
                    centerPointInWater = !centerPointInWater;
                }
            }
        } else {
            for (Integer edgeId : additionalEdges) {
                if (IntersectionHelper.arcsIntersect(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), latCenterPoint, lonCenterPoint,
                        originCenterPointLat, originCenterPointLon)) {
                    centerPointInWater = !centerPointInWater;
                }
            }
            for (int edgeId : edgeIds) {
                if (IntersectionHelper.arcsIntersect(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), latCenterPoint, lonCenterPoint,
                        originCenterPointLat, originCenterPointLon)) {
                    centerPointInWater = !centerPointInWater;
                }
            }
        }

        return centerPointInWater;
    }

    @Override
    public boolean isPointInWater(float lat, float lon) {
        boolean pointInWater = centerPointInWater;

        for (int i = 0; i < edgeCount; i++) {
            if (IntersectionHelper.arcsIntersect(lat, lon, latCenterPoint, lonCenterPoint,
                    CoastlineWays.getStartLatByEdgeIdx(i), CoastlineWays.getStartLonByEdgeIdx(i),
                    CoastlineWays.getDestLatByEdgeIdx(i), CoastlineWays.getDestLonByEdgeIdx(i))
            ) {
                pointInWater = !pointInWater;
            }
        }

        return pointInWater;
    }

    @Override
    public List<Integer> getAllContainedEdgeIDs() {
        List<Integer> list = Arrays.stream(edgeIds).boxed().collect(Collectors.toList());
        return list;
    }

    @Override
    public GridNode getCenterPoint() {
        return new GridNode(latCenterPoint, lonCenterPoint);
    }

    @Override
    public void getAllCenterPoints(int currDepth, int maxDepth, List<GridNode> pointList) {

        //List<GridNode> currList = new ArrayList<>();
        if(centerPointInWater ) { //|| !centerPointInWater
            pointList.add(this.getCenterPoint());
        }


        //return currList;

    }

}
