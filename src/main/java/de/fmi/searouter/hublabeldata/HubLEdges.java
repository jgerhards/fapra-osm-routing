package de.fmi.searouter.hublabeldata;

import de.fmi.searouter.hublablecreation.Edges;

/**
 * Contains edges relevant for the routing based on contraction hierarchies (meaning the two lowest levels). Since we
 * know the start node implicitly (based on the index), only the destination and distance are included.
 */
public class HubLEdges {
    //contains destinations of edges
    private static int[] dest;
    //contains distances of edges
    private static int[] dist;
    //index of first shortcut edge
    private static int firstShortcutIdx;
    //edges contained in a shortcut edge
    private static int[] shortcutParts;

    /**
     * Gets destination node of an edge.
     * @param edgeId the id of the edge
     * @return the destination node
     */
    public static int getDest(int edgeId) {
        return dest[edgeId];
    }

    /**
     * Gets the distance of an edge.
     * @param edgeId the id of the edge
     * @return the distance of the edge
     */
    public static int getDist(int edgeId) {
        return dist[edgeId];
    }

    /**
     * Checks if a given edge is a shortcut.
     * @param edgeId the id of the edge to check
     * @return true if it is a shortcut, else false
     */
    public static boolean isShortcut(int edgeId) {
        return edgeId >= firstShortcutIdx;
    }

    /**
     * Get the second part of a shortcut edge. This refers to the edge whose destination is the same
     * as that of the shortcut edge.
     * @param edgeId the id of the shortcut edge
     * @return the second edge used in this shortcut
     */
    public static int getSecondShortcut(int edgeId) {
        int idx = (edgeId - firstShortcutIdx) * 2;
        return shortcutParts[idx];
    }

    /**
     * Get the first part of a shortcut edge. This refers to the edge whose start is the same
     * as that of the shortcut edge.
     * @param edgeId the id of the shortcut edge
     * @return the first edge used in this shortcut
     */
    public static int getFirstShortcut(int edgeId) {
        int idx = ((edgeId - firstShortcutIdx) * 2) + 1;
        return shortcutParts[idx];
    }

    /**
     * Initializes data related to edges used by the hub label algorithm in order to be stored for later use.
     */
    public static void initialize() {
        int edgeCount = Edges.getNextShortcutId();
        dest = new int[edgeCount];
        dist = new int[edgeCount];
        int originalEdgeCount = Edges.getNumOfOriginalEdges();
        firstShortcutIdx = originalEdgeCount;
        int shortcutCount = edgeCount - originalEdgeCount;
        shortcutParts = new int[shortcutCount * 2];  //every shortcut contains two lower level edges

        int[] tmpDest = Edges.getOriginalEdgeDest();
        int[] tmpDist = Edges.getOriginalEdgeDist();
        System.arraycopy(tmpDest, 0, dest, 0, originalEdgeCount);
        System.arraycopy(tmpDist, 0, dist, 0, originalEdgeCount);

        tmpDest = Edges.getShortcutEdgeDest();
        tmpDist = Edges.getShortcutEdgeDist();
        System.arraycopy(tmpDest, 0, dest, originalEdgeCount, edgeCount - originalEdgeCount);
        System.arraycopy(tmpDist, 0, dist, originalEdgeCount, edgeCount - originalEdgeCount);

        int[] tmpShortcutParts = Edges.getShortcutEdgeParts();
        System.arraycopy(tmpShortcutParts, 0, shortcutParts, 0, (shortcutCount * 2));
    }

    //simple getters and setters for fields. Used when serializing or deserializing data.

    public static int[] getDest() {
        return dest;
    }

    public static int[] getDist() {
        return dist;
    }

    public static int getFirstShortcutIdx() {
        return firstShortcutIdx;
    }

    public static int[] getShortcutParts() {
        return shortcutParts;
    }

    public static void setDest(int[] dest) {
        HubLEdges.dest = dest;
    }

    public static void setDist(int[] dist) {
        HubLEdges.dist = dist;
    }

    public static void setFirstShortcutIdx(int firstShortcutIdx) {
        HubLEdges.firstShortcutIdx = firstShortcutIdx;
    }

    public static void setShortcutParts(int[] shortcutParts) {
        HubLEdges.shortcutParts = shortcutParts;
    }
}
