package de.fmi.searouter.grid;

public class Edge {
    private static int[] startNode;
    private static int[] destNode;
    private static int[] dist;

    public static int getStart(int i) {
        return startNode[i];
    }

    public static int getDest(int i) {
        return destNode[i];
    }

    public static int getDist(int i) {
        return dist[i];
    }
}

