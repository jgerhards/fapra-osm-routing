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

    public static int getStart(int i) {
        // todo: is this function even needed? find out by using **debug output**
        System.out.println("ttt: getStart used, do not delete");
        if(i < numOfOriginalEdges) {
            return originalEdgeStart[i];
        } else {
            int shortcutEdgeIdx = i - numOfOriginalEdges;
            return shortcutEdgeStart[shortcutEdgeIdx];
        }
    }

    public static int getDest(int i) {
        if(i < numOfOriginalEdges) {
            return originalEdgeDest[i];
        } else {
            int shortcutEdgeIdx = i - numOfOriginalEdges;
            return shortcutEdgeDest[shortcutEdgeIdx];
        }
    }

    public static int getDist(int i) {
        if(i < numOfOriginalEdges) {
            return originalEdgeDist[i];
        } else {
            int shortcutEdgeIdx = i - numOfOriginalEdges;
            return shortcutEdgeDist[shortcutEdgeIdx];
        }
    }

    public static void initializeForShortcutEdges(int numOfOriginalEdges) {
        Edges.numOfOriginalEdges = numOfOriginalEdges;

        Edges.nextShortcutId = numOfOriginalEdges;
        //todo: this seems ok for now, maybe think about size increase more late on
        Edges.shortcutEdgeSizeIncrease = numOfOriginalEdges / 2;

        //initialize shortcut arrays
        Edges.shortcutEdgeStart = new int[shortcutEdgeSizeIncrease];
        Edges.shortcutEdgeDest = new int[shortcutEdgeSizeIncrease];
        Edges.shortcutEdgeDist = new int[shortcutEdgeSizeIncrease];
        Edges.shortcutEdgeParts = new int[shortcutEdgeSizeIncrease * 2];
    }

    public static int addShortcutEdge(int start, int dest, int dist, int firstEdgeUsed, int secondEdgeUsed) {
        int nextArrayPosition = nextShortcutId - numOfOriginalEdges;
        if(shortcutEdgeDist.length - 1 < nextArrayPosition) {
            grow();
        }
        shortcutEdgeStart[nextArrayPosition] = start;
        shortcutEdgeDest[nextArrayPosition] = dest;
        shortcutEdgeDist[nextArrayPosition] = dist;

        shortcutEdgeParts[nextArrayPosition * 2] = firstEdgeUsed;
        shortcutEdgeParts[(nextArrayPosition * 2) + 1] = secondEdgeUsed;

        int edgeId = nextShortcutId;
        nextShortcutId++;
        return edgeId;
    }

    private static void grow() {
        int oldLen = shortcutEdgeDist.length;
        shortcutEdgeStart = Arrays.copyOf(shortcutEdgeStart, oldLen + shortcutEdgeSizeIncrease);
        shortcutEdgeDest = Arrays.copyOf(shortcutEdgeDest, oldLen + shortcutEdgeSizeIncrease);
        shortcutEdgeDist = Arrays.copyOf(shortcutEdgeDist, oldLen + shortcutEdgeSizeIncrease);
        shortcutEdgeParts = Arrays.copyOf(shortcutEdgeParts, (oldLen + shortcutEdgeSizeIncrease) * 2);
    }

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
