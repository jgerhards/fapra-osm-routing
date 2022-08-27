package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.dijkstragrid.Node;

/**
 * Efficient storage for Node entities used while calculating contraction hierarchies as a first step to get hub labels.
 * todo: In contrast to {@link Node}, this class can track which nodes have been removed from the graph so far.
 * In addition, it can store the rank (in contraction hierarchies) of the nodes.
 */
public class Nodes {
    private static double[] latitude;
    private static double[] longitude;
    private static int[] levels;
    //private static boolean[] wasRemoved;

    /**
     * Returns the latitude of a node with the given id. If the node was removed, this function throws an exception.
     * @param i The id of the node
     * @return the latitude of the node
     * @throws IllegalStateException if the node was removed from the graph
     */
    public static double getLatitude(int i) {
        /*if(wasRemoved[i]) {
            throw new IllegalStateException();
        }*/
        return latitude[i];
    }

    /**
     * Returns the longitude of a node with the given id. If the node was removed, this function throws an exception.
     * @param i The id of the node
     * @return the longitude of the node
     * @throws IllegalStateException if the node was removed from the graph
     */
    public static double getLongitude(int i) {
        return longitude[i];
    }

    public static  int getNodeLvl(int nodeId) {
        return levels[nodeId];
    }

    /**
     * Checks if a node was removed from a graph.
     * @param i The id of the node
     * @return true if the node was removed, else false
     */
    /*public static boolean nodeWasRemoved(int i) {
        return wasRemoved[i];
    }*/

    /**
     * Marks a node as removed from the grid.
     * @param i The id of the node
     */
    /*public static void removeNode(int i) {
        wasRemoved[i] = true;
    }*/

    /**
     * Initializes tracking for which nodes were removed from the graph.
     * @param nodeCount The number of nodes to track
     */
    /*public static void initializeRemovedTracking(int nodeCount) {
        Nodes.wasRemoved = new boolean[nodeCount];
        Arrays.fill(wasRemoved, false);
    }*/

    public static void setNodeLevel(int node, int lvl) {
        levels[node] = lvl;
    }

    public static void initializeLvls(int nodeNum) {
        levels = new int[nodeNum];
    }

    public static void setLevels(int[] levels) {
        Nodes.levels = levels;
    }

    public static void setLatitude(double[] latitude) {
        Nodes.latitude = latitude;
    }

    public static void setLongitude(double[] longitude) {
        Nodes.longitude = longitude;
    }

    public static int getSize() {
        return Nodes.latitude.length;
    }

    public static double[] getLatitude() {
        return latitude;
    }

    public static double[] getLongitude() {
        return longitude;
    }

    public static int[] getLevels() {
        return levels;
    }
}
