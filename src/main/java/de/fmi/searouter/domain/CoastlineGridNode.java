package de.fmi.searouter.domain;

import java.util.List;

/**
 * Inner node of the grid for a coastline check. This type of sub-node contains an array of other nodes which partition
 * the area of this node. Also, it contains a level in order to find to which sub-node a given point belongs to.
 */
public class CoastlineGridNode extends CoastlineGridElement{

    //nodes in the next lower level
    private CoastlineGridElement[][] subNodes;
    //level used to determine which decimal places of the point coordinates are relevant for the next partition
    private int level;

    /**
     * creates a new CoastlineGridNode. This also recursively calls itself to generate the entire hierarchy.
     * @param level the level of this node
     * @param position the index of the position. Is used to determine the borders of the area
     * @param coastlineIDs a list of all coastlines within this area
     */
    CoastlineGridNode(int level, int position, List<Integer> coastlineIDs) {
        this.level = level;
        //todo: generate the sub-nodes (can be both leafs or inner nodes, check number of edges in each one.
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public boolean pointIsInWater(double longitude, double latitude) {
        return false;
    }
}
