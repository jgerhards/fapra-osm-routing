package de.fmi.searouter.hublablecreation;

import java.util.Arrays;

/**
 * Stores all edges of the graph. All data of one edge is accessible
 * using the same index. Contains both initial edges and shortcut edges (which can be added at runtime)
 */
public class Edges {

    // Start node id of initial non-shortcut edges
    private static int[] originalEdgeStart;
    // Destination node id of initial non-shortcut edges
    private static int[] originalEdgeDest;
    // Length of initial non-shortcut edges (length between startNode and destNode)
    private static int[] originalEdgeDist;
    // Number of edges which are not shortcut edges
    private static int numOfOriginalEdges;

    // Start node id of shortcut edges
    private static int[] shortcutEdgeStart;
    // Dest node id of shortcut edges
    private static int[] shortcutEdgeDest;
    // Length of this shortcut edges
    private static int[] shortcutEdgeDist;
    // Lower level edges that make up shortcut edges. Twice as long as other shortcut arrays,
    // as 2 edges make up a shortcut.
    private static int[] shortcutEdgeParts;
    // Index the next shortcut edge will be assigned
    private static int nextShortcutId;

    //size by which shortcut data structures will be increased when they are full
    private static int shortcutEdgeSizeIncrease;

    /**
     * Get the destination node id of an edge with a given id.
     * @param i id of the edge
     * @return the id of the destination node
     */
    public static int getDest(int i) {
        if(i < numOfOriginalEdges) {
            return originalEdgeDest[i];
        } else {
            int shortcutEdgeIdx = i - numOfOriginalEdges;
            return shortcutEdgeDest[shortcutEdgeIdx];
        }
    }

    /**
     * Get the distance node id of an edge with a given id.
     * @param i id of the edge
     * @return the distance of the edge
     */
    public static int getDist(int i) {
        if(i < numOfOriginalEdges) {
            return originalEdgeDist[i];
        } else {
            int shortcutEdgeIdx = i - numOfOriginalEdges;
            return shortcutEdgeDist[shortcutEdgeIdx];
        }
    }

    /**
     * Initialize for shortcut edges. This means that data structures required for processing shortcut edges are
     * created. Before this call, no shortcut edge should be added.
     * @param numOfOriginalEdges the number of original edges
     */
    public static void initializeForShortcutEdges(int numOfOriginalEdges) {
        Edges.numOfOriginalEdges = numOfOriginalEdges;
        Edges.nextShortcutId = numOfOriginalEdges;
        Edges.shortcutEdgeSizeIncrease = numOfOriginalEdges / 2;

        //initialize shortcut arrays
        Edges.shortcutEdgeStart = new int[shortcutEdgeSizeIncrease];
        Edges.shortcutEdgeDest = new int[shortcutEdgeSizeIncrease];
        Edges.shortcutEdgeDist = new int[shortcutEdgeSizeIncrease];
        Edges.shortcutEdgeParts = new int[shortcutEdgeSizeIncrease * 2];
    }

    /**
     * Add a shortcut edge.
     * @param start the id of the start node
     * @param dest the id of the destination node
     * @param dist the total distance of the shortcut edge
     * @param firstEdgeUsed the id of the first edge contained (start is same as shortcut edge)
     * @param secondEdgeUsed the id of the second edge contained (destination is same as shortcut edge)
     * @return the id of the new shortcut edge
     */
    public static int addShortcutEdge(int start, int dest, int dist, int firstEdgeUsed, int secondEdgeUsed) {
        int nextArrayPosition = nextShortcutId - numOfOriginalEdges;
        if(shortcutEdgeDist.length - 1 < nextArrayPosition) {
            grow();
        }
        shortcutEdgeStart[nextArrayPosition] = start;
        shortcutEdgeDest[nextArrayPosition] = dest;
        shortcutEdgeDist[nextArrayPosition] = dist;

        //shortcut parts have to account for the array being twice as long
        shortcutEdgeParts[nextArrayPosition * 2] = firstEdgeUsed;
        shortcutEdgeParts[(nextArrayPosition * 2) + 1] = secondEdgeUsed;

        int edgeId = nextShortcutId;
        nextShortcutId++;
        return edgeId;
    }

    /**
     * Increase the size of data structures to accommodate more shortcut edges.
     */
    private static void grow() {
        int oldLen = shortcutEdgeDist.length;
        shortcutEdgeStart = Arrays.copyOf(shortcutEdgeStart, oldLen + shortcutEdgeSizeIncrease);
        shortcutEdgeDest = Arrays.copyOf(shortcutEdgeDest, oldLen + shortcutEdgeSizeIncrease);
        shortcutEdgeDist = Arrays.copyOf(shortcutEdgeDist, oldLen + shortcutEdgeSizeIncrease);
        shortcutEdgeParts = Arrays.copyOf(shortcutEdgeParts, (oldLen + shortcutEdgeSizeIncrease) * 2);
    }

    //getters and setters for serialization and deserialization

    public static void setOriginalEdgeStart(int[] originalEdgeStart) {
        Edges.originalEdgeStart = originalEdgeStart;
    }

    public static void setOriginalEdgeDest(int[] originalEdgeDest) {
        Edges.originalEdgeDest = originalEdgeDest;
    }

    public static void setOriginalEdgeDist(int[] originalEdgeDist) {
        Edges.originalEdgeDist = originalEdgeDist;
    }

    public static void setNumOfOriginalEdges(int numOfOriginalEdges) {
        Edges.numOfOriginalEdges = numOfOriginalEdges;
    }

    public static void setShortcutEdgeStart(int[] shortcutEdgeStart) {
        Edges.shortcutEdgeStart = shortcutEdgeStart;
    }

    public static void setShortcutEdgeDest(int[] shortcutEdgeDest) {
        Edges.shortcutEdgeDest = shortcutEdgeDest;
    }

    public static void setShortcutEdgeDist(int[] shortcutEdgeDist) {
        Edges.shortcutEdgeDist = shortcutEdgeDist;
    }

    public static void setShortcutEdgeParts(int[] shortcutEdgeParts) {
        Edges.shortcutEdgeParts = shortcutEdgeParts;
    }

    public static void setNextShortcutId(int nextShortcutId) {
        Edges.nextShortcutId = nextShortcutId;
    }

    public static void setShortcutEdgeSizeIncrease(int shortcutEdgeSizeIncrease) {
        Edges.shortcutEdgeSizeIncrease = shortcutEdgeSizeIncrease;
    }

    public static int[] getOriginalEdgeStart() {
        return originalEdgeStart;
    }

    public static int[] getOriginalEdgeDest() {
        return originalEdgeDest;
    }

    public static int[] getOriginalEdgeDist() {
        return originalEdgeDist;
    }

    public static int getNumOfOriginalEdges() {
        return numOfOriginalEdges;
    }

    public static int[] getShortcutEdgeStart() {
        return shortcutEdgeStart;
    }

    public static int[] getShortcutEdgeDest() {
        return shortcutEdgeDest;
    }

    public static int[] getShortcutEdgeDist() {
        return shortcutEdgeDist;
    }

    public static int[] getShortcutEdgeParts() {
        return shortcutEdgeParts;
    }

    public static int getNextShortcutId() {
        return nextShortcutId;
    }

    public static int getShortcutEdgeSizeIncrease() {
        return shortcutEdgeSizeIncrease;
    }
}
