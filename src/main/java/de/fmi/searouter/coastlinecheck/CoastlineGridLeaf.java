package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.coastlinecheck.CoastlineGridElement;

import java.util.Arrays;
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
        this.refPointLatitude = refPointLatitude;
        this.refPointLongitude = refPointLongitude;

        coastlineWayIDs = new int[coastlineIDs.size()];
        //move list to array manually to ensure only primitives are used in the array
        for(int i = 0; i < coastlineIDs.size(); i++) {
            coastlineWayIDs[i] = coastlineIDs.get(i);
        }
        //todo: add information if reference point is in water. Maybe use separate function for this?
    }

    @Override
    public boolean isLeafNode() {
        return true;
    }

    @Override
    public boolean pointIsInWater(double longitude, double latitude) {
        return false;
    }
}
