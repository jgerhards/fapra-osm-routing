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

        if(DoubleMath.fuzzyEquals(latCenterPoint, 55.0, 0.001) && DoubleMath.fuzzyEquals(lonCenterPoint, 		-163.8889, 0.001)) {
            int breakpoint = 1;
            System.out.println("ppp: set center leaf");
        }
    }

    @Override
    public boolean initCenterPoint(double originCenterPointLat, double originCenterPointLon,
                                boolean originCenterPointInWater, Set<Integer> allEdgeIds,
                                ApproachDirection dir) {
        boolean printIntersects = false;
        if(DoubleMath.fuzzyEquals(latCenterPoint, 		55.0, 0.001) && DoubleMath.fuzzyEquals(lonCenterPoint, 		-163.8889, 0.001)) {
            int breakpoint = 1;
            System.out.println("ppp: init center leaf: " + latCenterPoint + ", " + lonCenterPoint +
                    " origin: " + originCenterPointLat + " " + originCenterPointLon +
             " direction: " + dir + " origin in water: " + originCenterPointInWater);
            printIntersects = true;
            System.out.println("ppp: contains before: " + allEdgeIds.contains(47796314));

        }
        centerPointInWater = originCenterPointInWater;
        for (int edgeId : this.edgeIds) {
            allEdgeIds.add(edgeId);
        }
        if(printIntersects) {
            System.out.println("ppp: contains after: " + allEdgeIds.contains(47796314));
        }
        if(dir == ApproachDirection.FROM_HORIZONTAL) {
            boolean noException;
            do {
                boolean backupInWater = centerPointInWater;
                noException = true;
                try {
                    for (Integer edgeId : allEdgeIds) {
                        if (IntersectionHelper.crossesLatitudeWithException(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                                CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                                CoastlineWays.getDestLonByEdgeIdx(edgeId), latCenterPoint,
                                originCenterPointLon, lonCenterPoint)) {
                            centerPointInWater = !centerPointInWater;
                            if(printIntersects) {
                    /*System.out.println("ppp: intersect: " + edgeId + " " + CoastlineWays.getStartLatByEdgeIdx(edgeId) +
                            " " + CoastlineWays.getStartLonByEdgeIdx(edgeId) +
                            " " + CoastlineWays.getDestLatByEdgeIdx(edgeId) +
                            " " + CoastlineWays.getDestLonByEdgeIdx(edgeId));*/

                                System.out.println("    {\n" +
                                        "      \"type\": \"Feature\",\n" +
                                        "      \"properties\": {},\n" +
                                        "      \"geometry\": {\n" +
                                        "        \"type\": \"LineString\",\n" +
                                        "        \"coordinates\": [\n" +
                                        "          [\n" + CoastlineWays.getStartLonByEdgeIdx(edgeId) + ", " + CoastlineWays.getStartLatByEdgeIdx(edgeId)
                                        + "], [" + CoastlineWays.getDestLonByEdgeIdx(edgeId) +
                                        ", " + CoastlineWays.getDestLatByEdgeIdx(edgeId) + "          ]\n" +
                                        "        ]\n" +
                                        "      }\n" +
                                        "    },\n");
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("ttt: caught exception 3");
                    latCenterPoint += 0.000001;
                    noException = false;
                    centerPointInWater = backupInWater;
                }
            } while(!noException);
        } else {
            boolean noException;
            do {
                boolean backupInWater = centerPointInWater;
                noException = true;
                try {
                    for (Integer edgeId : allEdgeIds) {
                        if (IntersectionHelper.arcsIntersectWithException(CoastlineWays.getStartLatByEdgeIdx(edgeId),
                                CoastlineWays.getStartLonByEdgeIdx(edgeId), CoastlineWays.getDestLatByEdgeIdx(edgeId),
                                CoastlineWays.getDestLonByEdgeIdx(edgeId),
                                originCenterPointLat, originCenterPointLon, latCenterPoint, lonCenterPoint)) {
                            centerPointInWater = !centerPointInWater;
                            if(printIntersects) {
                        /*System.out.println("ppp: intersect: " + edgeId + " " + CoastlineWays.getStartLatByEdgeIdx(edgeId) +
                                " " + CoastlineWays.getStartLonByEdgeIdx(edgeId) +
                                " " + CoastlineWays.getDestLatByEdgeIdx(edgeId) +
                                " " + CoastlineWays.getDestLonByEdgeIdx(edgeId));*/

                                System.out.println("    {\n" +
                                        "      \"type\": \"Feature\",\n" +
                                        "      \"properties\": {},\n" +
                                        "      \"geometry\": {\n" +
                                        "        \"type\": \"LineString\",\n" +
                                        "        \"coordinates\": [\n" +
                                        "          [\n" + CoastlineWays.getStartLonByEdgeIdx(edgeId) + ", " + CoastlineWays.getStartLatByEdgeIdx(edgeId)
                                        + "], [" + CoastlineWays.getDestLonByEdgeIdx(edgeId) +
                                        ", " + CoastlineWays.getDestLatByEdgeIdx(edgeId) + "          ]\n" +
                                        "        ]\n" +
                                        "      }\n" +
                                        "    },\n");
                            }
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
