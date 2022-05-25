package de.fmi.searouter.coastlinecheck;

/**
 * an element of the grid used to partition the map for a coastline check.
 */
public abstract class CoastlineGridElement {
    public static final int MAX_COASTLINES_IN_GRID = 500;

    /**
     * check if this CoastlineGridElement is a leaf node or an inner node.
     * @return true if this is a leaf node, else false
     */
    //todo: can this function be removed?
    public abstract boolean isLeafNode();

    /**
     * check if a given point is located on land or in water.
     * @param latitude of the point
     * @param longitude of the point
     * @return true if the point is in water, else false
     */
    public abstract boolean pointIsInWater(double latitude, double longitude);
}
