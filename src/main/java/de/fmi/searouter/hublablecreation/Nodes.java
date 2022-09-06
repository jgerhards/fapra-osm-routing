package de.fmi.searouter.hublablecreation;

/**
 * Efficient storage for Node entities used while calculating hub label data.
 * In addition, it can store the rank (in contraction hierarchies) of the nodes.
 */
public class Nodes {
    //coordinates
    private static double[] latitude;
    private static double[] longitude;

    //levels of the nodes
    private static int[] levels;

    /**
     * Return the latitude of a node with a given id.
     * @param i the id of the node
     * @return the latitude of the node
     */
    public static double getLatitude(int i) {
        return latitude[i];
    }

    /**
     * Return the longitude of a node with a given id.
     * @param i the id of the node
     * @return the longitude of the node
     */
    public static double getLongitude(int i) {
        return longitude[i];
    }

    /**
     * Return the level of a node with a given id.
     * @param nodeId the id of the node
     * @return the level of the node
     */
    public static  int getNodeLvl(int nodeId) {
        return levels[nodeId];
    }

    /**
     * Set the level of a node with a given id.
     * @param node the id of the node to set the level of
     * @param lvl the level to set the node to
     */
    public static void setNodeLevel(int node, int lvl) {
        levels[node] = lvl;
    }

    /**
     * Initialize internal data structures to accommodate level information for nodes.
     * @param nodeNum the total number of nodes
     */
    public static void initializeLvls(int nodeNum) {
        levels = new int[nodeNum];
    }

    /**
     * Get the number of nodes stored.
     * @return the number of nodes
     */
    public static int getNodeCount() {
        //note: longitude array would also work, there is no particular reason latitude is used
        return Nodes.latitude.length;
    }

    //simple getters and setters for fields. Used when serializing or deserializing data.

    public static void setLevels(int[] levels) {
        Nodes.levels = levels;
    }

    public static void setLatitude(double[] latitude) {
        Nodes.latitude = latitude;
    }

    public static void setLongitude(double[] longitude) {
        Nodes.longitude = longitude;
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
