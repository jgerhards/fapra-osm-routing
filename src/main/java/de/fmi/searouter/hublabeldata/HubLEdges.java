package de.fmi.searouter.hublabeldata;

import de.fmi.searouter.hublablecreation.Edges;

public class HubLEdges {
    private static int[] dest;
    private static int[] dist;
    private static int firstShortcutIdx;
    private static int[] shortcutParts;

    public static int getDest(int edgeId) {
        return dest[edgeId];
    }

    public static int getDist(int edgeId) {
        return dist[edgeId];
    }

    public static boolean isShortcut(int edgeId) {
        return edgeId >= firstShortcutIdx;
    }

    public static int getSecondShortcut(int edgeId) {
        int idx = (edgeId - firstShortcutIdx) * 2;
        return shortcutParts[idx];
    }

    public static int getFirstShortcut(int edgeId) {
        int idx = ((edgeId - firstShortcutIdx) * 2) + 1;
        return shortcutParts[idx];
    }

    public static void initialize() {
        int edgeCount = Edges.getNextShortcutId();
        dest = new int[edgeCount];
        dist = new int[edgeCount];
        int originalEdgeCount = Edges.getNumOfOriginalEdges();
        firstShortcutIdx = originalEdgeCount;
        int shortcutCount = edgeCount - originalEdgeCount;
        shortcutParts = new int[shortcutCount * 2];

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
