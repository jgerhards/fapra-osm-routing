package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.domain.IntersectionHelper;

import java.util.List;

/**
 * leaf node of the grid for a coastline check. Due to being a leaf node, no further partition of this node is done.
 * Instead, a center point is contained which can be used to check if an arbitrary point is in water or on land.
 */
public class CoastlineGridLeaf extends CoastlineGridElement {
    //describe a reference point within the area to use for determining if a given point is on land or in the water
    private double refPointLongitude;
    private double refPointLatitude;
    private boolean refPointIsInWater;

    //array containing IDs of all coastline ways to be considered in this area
    private int[] coastlineWayIDs;

    CoastlineGridLeaf(double refPointLatitude, double refPointLongitude, List<Integer> coastlineIDs) {
        System.out.println("constructor of grid leaf called, list size = " + coastlineIDs.size());
        this.refPointLatitude = refPointLatitude;
        this.refPointLongitude = refPointLongitude;

        coastlineWayIDs = new int[coastlineIDs.size()];
        //move list to array manually to ensure only primitives are used in the array
        for(int i = 0; i < coastlineIDs.size(); i++) {
            coastlineWayIDs[i] = coastlineIDs.get(i);
        }
        //todo: below is a very suboptimal implementation. May be a good idea to look over this again if it is too slow
        initializeReferencePoint();
    }

    /**
     * Check if the reference point of this node is in water or on land. Currently called from the constructor,
     * but public as it may be called from another source later on (using a more efficient implementation).
     */
    public void initializeReferencePoint() {
        //todo: naive algorithm used --> currently much more work-intensive than necessary
        int numberOfCoastlines = Coastlines.getNumberOfWays();
        //start of the same as the global reference, negate on intersecting a coastline
        refPointIsInWater = Coastlines.GLOBAL_REFERENCE_IN_WATER;
        for(int i = 0; i < numberOfCoastlines; i++) {
            double coastlineStartLat = Coastlines.getStartLatitude(i);
            double coastlineStartLong = Coastlines.getStartLatitude(i);
            double coastlineEndLat = Coastlines.getStartLatitude(i);
            double coastlineEndLong = Coastlines.getStartLatitude(i);

            boolean linesIntersect = IntersectionHelper.linesIntersect(refPointLatitude, refPointLongitude,
                    Coastlines.GLOBAL_REFERENCE_LATITUDE, Coastlines.GLOBAL_REFERENCE_LONGITUDE,
                    coastlineStartLat, coastlineStartLong, coastlineEndLat, coastlineEndLong);
            if(linesIntersect) {
                refPointIsInWater = !refPointIsInWater;
            }
        }
    }

    @Override
    public boolean isLeafNode() {
        return true;
    }

    @Override
    public boolean pointIsInWater(double latitude, double longitude) {
        //start of the same as the local reference point, negate on intersecting a coastline
        //todo: implement special case where a start/end point is on the line --> add random point y and test x->y->ref
        boolean pointInWater = refPointIsInWater;
        for(int i : coastlineWayIDs) {
            double coastlineStartLat = Coastlines.getStartLatitude(i);
            double coastlineStartLong = Coastlines.getStartLatitude(i);
            double coastlineEndLat = Coastlines.getStartLatitude(i);
            double coastlineEndLong = Coastlines.getStartLatitude(i);

            boolean linesIntersect = IntersectionHelper.linesIntersect(latitude, longitude, refPointLatitude,
                    refPointLongitude, coastlineStartLat, coastlineStartLong, coastlineEndLat, coastlineEndLong);
            if(linesIntersect) {
                pointInWater = !pointInWater;
            }
        }
        return pointInWater;
    }
}
