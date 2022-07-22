package de.fmi.searouter.coastlinegrid;

import com.google.common.math.DoubleMath;
import de.fmi.searouter.dijkstragrid.GridNode;
import de.fmi.searouter.importdata.Point;
import de.fmi.searouter.utils.IntersectionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

        if(DoubleMath.fuzzyEquals(lat, 	28.2099, 0.1) && DoubleMath.fuzzyEquals(lon, -177.3868, 0.1)) {
            int breakpoint = 1;
            System.out.println("a");
        }
    }

    @Override
    public boolean initCenterPoint(double originCenterPointLat, double originCenterPointLon,
                                boolean originCenterPointInWater, Set<Integer> allEdgeIds,
                                ApproachDirection dir) {
        if(DoubleMath.fuzzyEquals(latCenterPoint, 	28.2099, 0.1) && DoubleMath.fuzzyEquals(lonCenterPoint, -177.3868, 0.1)) {
            int breakpoint = 1;
            System.out.println("a");
        }
        centerPointInWater = originCenterPointInWater;
        for (int edgeId : this.edgeIds) {
            allEdgeIds.add(edgeId);
        }
        if(dir == ApproachDirection.FROM_HORIZONTAL) {
            for (Integer edgeId : allEdgeIds) {
                if (IntersectionHelper.crossesLatitude(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                        CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                        CoastlineWays.getDestLonByEdgeIdx(edgeId), latCenterPoint,
                        originCenterPointLon, lonCenterPoint)) {
                    centerPointInWater = !centerPointInWater;
                }
            }
        } else {
            boolean noException;
            do {
                boolean backupInWater = centerPointInWater;
                noException = true;
                try {
                    for (Integer edgeId : allEdgeIds) {
                        if (IntersectionHelper.arcsIntersectWithException(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                                CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                                CoastlineWays.getDestLonByEdgeIdx(edgeId), latCenterPoint, lonCenterPoint,
                                originCenterPointLat, originCenterPointLon)) {
                            centerPointInWater = !centerPointInWater;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("ttt: caught exception");
                    lonCenterPoint += 0.000001;
                    noException = false;
                    centerPointInWater = backupInWater;
                }
            } while(!noException);
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
    public Set<Integer> getAllContainedEdgeIDs() {
        Set<Integer> list = Arrays.stream(edgeIds).boxed().collect(Collectors.toSet());
        return list;
    }

    @Override
    public GridNode getCenterPoint() {
        return new GridNode(latCenterPoint, lonCenterPoint);
    }

    @Override
    public void getAllCenterPoints(int currDepth, int maxDepth, List<GridNode> pointList) {

        //List<GridNode> currList = new ArrayList<>();
        if(!centerPointInWater || !centerPointInWater) { //
            pointList.add(this.getCenterPoint());
        }


        //return currList;

    }

    @Override
    public double getCtrLat() {
        return latCenterPoint;
    }

    @Override
    public double getCtrLon() {
        return lonCenterPoint;
    }

}
