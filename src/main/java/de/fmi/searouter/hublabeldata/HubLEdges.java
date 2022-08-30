package de.fmi.searouter.hublabeldata;

import de.fmi.searouter.hublablecreation.Edges;

public class HubLEdges {
    private static int[] dest;
    private static int[] dist;
    private static int firstShortcutIdx;
    private static int[] shortcutParts;

    public static void initialize() {
        int edgeCount = Edges.getNextShortcutId();
        dest = new int[edgeCount];
        dist = new int[edgeCount];
        int originalEdgeCount = Edges.getNumOfOriginalEdges();
        firstShortcutIdx = originalEdgeCount;
        int shortcutCount = edgeCount - originalEdgeCount;
        shortcutParts = new int[shortcutCount];

        int[] tmpDest = Edges.getOriginalEdgeDest();
        int[] tmpDist = Edges.getOriginalEdgeDist();
        System.arraycopy(tmpDest, 0, dest, 0, originalEdgeCount);
        System.arraycopy(tmpDist, 0, dist, 0, originalEdgeCount);

        tmpDest = Edges.getShortcutEdgeDest();
        tmpDist = Edges.getShortcutEdgeDist();
        System.arraycopy(tmpDest, 0, dest, originalEdgeCount, edgeCount - originalEdgeCount);
        System.arraycopy(tmpDist, 0, dist, originalEdgeCount, edgeCount - originalEdgeCount);

        int[] tmpShortcutParts = Edges.getShortcutEdgeParts();
        System.arraycopy(tmpShortcutParts, 0, shortcutParts, 0, shortcutCount);
    }
}
