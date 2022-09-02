package de.fmi.searouter.hublabeldata;

import de.fmi.searouter.hublablecreation.DynamicGrid;
import de.fmi.searouter.hublablecreation.Edges;
import de.fmi.searouter.hublablecreation.Labels;
import de.fmi.searouter.hublablecreation.Nodes;

import java.io.*;
import java.util.Arrays;
import java.util.stream.IntStream;

public class HubLNodes {
    private static int hlLevel;
    private static double[] longitudes;
    private static double[] latitudes;
    private static int[] levels;
    private static int[] edgesOffset;
    private static int[] edges;
    private static int[] labelOffset;
    private static int[] labelNode;
    private static int[] labelEdge;
    private static int[] labelDist;

    public static int getNumOfNodes() {
        return longitudes.length;
    }

    public static double getLat(int nodeID) {
        return latitudes[nodeID];
    }

    public static double getLong(int nodeID) {
        return longitudes[nodeID];
    }

    public static double getLvl(int nodeID) {
        return levels[nodeID];
    }

    public static int getLabelNode(int nodeId) {
        return labelNode[nodeId];
    }

    public static int getLabelEdge(int nodeId) {
        return labelEdge[nodeId];
    }

    public static int getLabelDist(int nodeId) {
        return labelDist[nodeId];
    }

    public static boolean nodeHasLabels(int nodeId) {
        return levels[nodeId] >= hlLevel;
    }

    public static int getLabelOffset(int nodeId) {
        return labelOffset[nodeId];
    }

    public static int getEdgeOffset(int nodeId) {
        return edgesOffset[nodeId];
    }

    public static int getEdge(int edgeIdx) {
        return edges[edgeIdx];
    }

    public static void initHlLvl(int lvl) {
        hlLevel = lvl;
    }

    public static void initNodeData() {
        longitudes = Nodes.getLongitude();
        latitudes = Nodes.getLatitude();
        levels = Nodes.getLevels();

        //save heap space
        Nodes.setLongitude(null);
        Nodes.setLatitude(null);
        Nodes.setLevels(null);
    }

    public static void initEdgeInfo() {
        if (longitudes == null) {
            throw new IllegalStateException();
        }

        //remove all unnecessary edges and store the others
        int[][] currentGrid = DynamicGrid.getAllEdgeIds();
        int[] edgeCounts = DynamicGrid.getAllEdgeCount();
        DynamicGrid.setAllEdgeIds(null);
        DynamicGrid.setAllEdgeCount(null);
        int nodeCount = edgeCounts.length;
        int totalEdgeLen = 0;
        for (int i = 0; i < nodeCount; i++) {
            int nodeLvl = levels[i];
            if(nodeLvl >= hlLevel) {
                currentGrid[i] = null;
                edgeCounts[i] = 0;
            } else {
                int edgeCount = edgeCounts[i];
                int j = 0;
                while(j < edgeCount) {
                    int[] edgeArray = currentGrid[i];
                    int destLvl = levels[Edges.getDest(edgeArray[j])];
                    if(destLvl <= nodeLvl) {
                        //delete edge
                        edgeCount--;
                        System.arraycopy(edgeArray, j + 1, edgeArray, j, edgeCount - j);
                    } else {
                        totalEdgeLen++;
                        j++;
                    }
                }
                edgeCounts[i] = edgeCount;
            }
        }

        edgesOffset = new int[nodeCount + 1];
        edges = new int[totalEdgeLen];
        int nextOffset = 0;
        for (int i = 0; i < nodeCount; i++) {
            int nextEdgeCount = edgeCounts[i];
            edgesOffset[i] = nextOffset;
            if (nextEdgeCount != 0) {
                System.arraycopy(currentGrid[i], 0, edges, nextOffset, nextEdgeCount);
                nextOffset += nextEdgeCount;
            }
        }
        edgesOffset[nodeCount] = nextOffset;
     }

    public static void initLabelInfo() throws IOException {
        int[][] tmpLabelNodes = Labels.getLabelNodes();
        int[][] tmpLabelEdges = Labels.getLabelEdges();
        int[][] tmpLabelDist = Labels.getLabelDist();
        Labels.setLabelNodes(null);
        Labels.setLabelEdges(null);
        Labels.setLabelDist(null);
        String filePath = "tmp_HL_file";
        File f = new File(filePath);
        f.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));

        int nodeCount = tmpLabelNodes.length;
        labelOffset = new int[nodeCount + 1];
        int nextOffset = 0;
        for (int i = 0; i < nodeCount; i++) {
            labelOffset[i] = nextOffset;
            if (tmpLabelNodes[i] != null) {
                int labelCount = tmpLabelNodes[i].length;
                int[] nodes = tmpLabelNodes[i];
                int[] edges = tmpLabelEdges[i];
                int[] dist = tmpLabelDist[i];
                if(!(IntStream.range(0, nodes.length - 1).noneMatch(idx -> nodes[idx] > nodes[idx + 1]))) {
                    System.out.println("ttt: error unsorted array");
                    System.exit(-1);
                }
                for (int j = 0; j < labelCount; j++) {
                    writer.write(nodes[j] + "\n");
                    writer.write(edges[j] + "\n");
                    writer.write(dist[j] + "\n");
                }
                nextOffset += labelCount;
            }
        }
        labelOffset[nodeCount] = nextOffset;
        writer.close();
        //free up memory
        tmpLabelNodes = null;
        tmpLabelEdges = null;
        tmpLabelDist = null;

        labelNode = new int[nextOffset + 1];
        labelEdge = new int[nextOffset + 1];
        labelDist = new int[nextOffset + 1];
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        for (int i = 0; i < nextOffset; i++) { //nextOffset is equal to the label count now
            labelNode[i] = Integer.parseInt(reader.readLine());
            labelEdge[i] = Integer.parseInt(reader.readLine());
            labelDist[i] = Integer.parseInt(reader.readLine());
        }
        labelNode[nextOffset] = -1;
        labelEdge[nextOffset] = -1;
        labelDist[nextOffset] = -1;
        reader.close();
    }

    public static int getHlLevel() {
        return hlLevel;
    }

    public static double[] getLongitudes() {
        return longitudes;
    }

    public static double[] getLatitudes() {
        return latitudes;
    }

    public static int[] getLevels() {
        return levels;
    }

    public static int[] getEdgesOffset() {
        return edgesOffset;
    }

    public static int[] getEdges() {
        return edges;
    }

    public static int[] getLabelOffset() {
        return labelOffset;
    }

    public static int[] getLabelNode() {
        return labelNode;
    }

    public static int[] getLabelEdge() {
        return labelEdge;
    }

    public static int[] getLabelDist() {
        return labelDist;
    }

    public static void setHlLevel(int hlLevel) {
        HubLNodes.hlLevel = hlLevel;
    }

    public static void setLongitudes(double[] longitudes) {
        HubLNodes.longitudes = longitudes;
    }

    public static void setLatitudes(double[] latitudes) {
        HubLNodes.latitudes = latitudes;
    }

    public static void setLevels(int[] levels) {
        HubLNodes.levels = levels;
    }

    public static void setEdgesOffset(int[] edgesOffset) {
        HubLNodes.edgesOffset = edgesOffset;
    }

    public static void setEdges(int[] edges) {
        HubLNodes.edges = edges;
    }

    public static void setLabelOffset(int[] labelOffset) {
        HubLNodes.labelOffset = labelOffset;
    }

    public static void setLabelNode(int[] labelNode) {
        HubLNodes.labelNode = labelNode;
    }

    public static void setLabelEdge(int[] labelEdge) {
        HubLNodes.labelEdge = labelEdge;
    }

    public static void setLabelDist(int[] labelDist) {
        HubLNodes.labelDist = labelDist;
    }
}
