package de.fmi.searouter.coastlinecheck;

/**
 * an element of the grid used to partition the map for a coastline check.
 */
public abstract class CoastlineGridElement {
    public static final int MAX_COASTLINES_IN_GRID = 20;

    /**
     * check if this CoastlineGridElement is a leaf node or an inner node.
     * @return true if this is a leaf node, else false
     */
    public abstract boolean isLeafNode();

    /**
     * check if a given point is loacated on land or in water.
     * @param longitude of the point
     * @param latitude of the point
     * @return true if the point is in water, else false
     */
    public abstract boolean pointIsInWater(double longitude, double latitude);
}
