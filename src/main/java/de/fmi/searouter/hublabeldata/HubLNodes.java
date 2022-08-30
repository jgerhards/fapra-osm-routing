package de.fmi.searouter.hublabeldata;

import de.fmi.searouter.dijkstragrid.Edge;
import de.fmi.searouter.dijkstragrid.Node;
import de.fmi.searouter.hublablecreation.DynamicGrid;
import de.fmi.searouter.hublablecreation.Edges;
import de.fmi.searouter.hublablecreation.Labels;
import de.fmi.searouter.hublablecreation.Nodes;

import java.io.*;
import java.util.Arrays;

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

        edgesOffset = new int[nodeCount];
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
        labelOffset = new int[nodeCount];
        int nextOffset = 0;
        for (int i = 0; i < nodeCount; i++) {
            labelOffset[i] = nextOffset;
            if (tmpLabelNodes[i] != null) {
                int labelCount = tmpLabelNodes[i].length;
                int[] nodes = tmpLabelNodes[i];
                int[] edges = tmpLabelEdges[i];
                int[] dist = tmpLabelDist[i];
                for (int j = 0; j < labelCount; j++) {
                    writer.write(nodes[j] + "\n");
                    writer.write(edges[j] + "\n");
                    writer.write(dist[j] + "\n");
                }
                nextOffset += labelCount;
            }
        }
        writer.close();
        //free up memory
        tmpLabelNodes = null;
        tmpLabelEdges = null;
        tmpLabelDist = null;

        labelNode = new int[nextOffset];
        labelEdge = new int[nextOffset];
        labelDist = new int[nextOffset];
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        for (int i = 0; i < nextOffset; i++) { //nextOffset is equal to the label count now
            labelNode[i] = Integer.parseInt(reader.readLine());
            labelEdge[i] = Integer.parseInt(reader.readLine());
            labelDist[i] = Integer.parseInt(reader.readLine());
        }
        reader.close();
    }
}
