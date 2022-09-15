package de.fmi.searouter.hublabeldata;

import de.fmi.searouter.hublablecreation.DynamicGrid;
import de.fmi.searouter.hublablecreation.Edges;
import de.fmi.searouter.hublablecreation.Labels;
import de.fmi.searouter.hublablecreation.Nodes;

import java.io.*;

/**
 * Contains data about nodes relevant for the hub label routing algorithm.
 */
public class HubLNodes {
    //the first level in which hub labels are present
    private static int hlLevel;
    //the levels of the nodes
    private static int[] levels;

    //coordinate data
    private static double[] longitudes;
    private static double[] latitudes;

    //offsets in the edges array based on id of nodes
    private static int[] edgesOffset;
    //edge ids associated with lower level nodes for contraction hierarchies
    private static int[] edges;

    //offsets in the arrays containing label data based on id of nodes
    private static int[] labelOffset;
    //label data, containing the node, the first edge in that direction and the total distance to the label node
    private static int[] labelNode;
    private static int[] labelEdge;
    private static int[] labelDist;

    /**
     * Get the total number of nodes.
     * @return the number of nodes
     */
    public static int getNumOfNodes() {
        return longitudes.length;
    }

    /**
     * Get the latitude of a given node.
     * @param nodeID the id of the node
     * @return the latitude of the node
     */
    public static double getLat(int nodeID) {
        return latitudes[nodeID];
    }

    /**
     * Get the longitude of a given node.
     * @param nodeID the id of the node
     * @return the longitude of the node
     */
    public static double getLong(int nodeID) {
        return longitudes[nodeID];
    }

    /**
     * Get the node associated with a given label.
     * @param idx the idx of the label
     * @return the node associated with the label
     */
    public static int getLabelNode(int idx) {
        return labelNode[idx];
    }

    /**
     * Get the edge associated with a given label.
     * @param idx the idx of the label
     * @return the edge associated with the label
     */
    public static int getLabelEdge(int idx) {
        return labelEdge[idx];
    }

    /**
     * Get the distance associated with a given label.
     * @param idx the idx of the label
     * @return the distance associated with the label
     */
    public static int getLabelDist(int idx) {
        return labelDist[idx];
    }

    /**
     * Check if a given node has labels. If that is not the case, edge data is stored for this node
     * instead of label data.
     * @param nodeId the id of the node to check
     * @return true if label data is available, else false
     */
    public static boolean nodeHasLabels(int nodeId) {
        return levels[nodeId] >= hlLevel;
    }

    /**
     * Get the label offset of a given node.
     * @param nodeId the id of the node
     * @return the label offset (index) of the node
     */
    public static int getLabelOffset(int nodeId) {
        return labelOffset[nodeId];
    }

    /**
     * Get the edge offset of a given node.
     * @param nodeId the id of the node
     * @return the edge offset (index) of the node
     */
    public static int getEdgeOffset(int nodeId) {
        return edgesOffset[nodeId];
    }

    /**
     * Get the edge at a given index.
     * @param idx the idx of the edge
     * @return the edge id stored at this index
     */
    public static int getEdge(int idx) {
        return edges[idx];
    }

    /**
     * Initialize the level at which labels are available.
     * @param lvl the level at which labels are available
     */
    public static void initHlLvl(int lvl) {
        hlLevel = lvl;
    }

    /**
     * Initializes data related to nodes used by the hub label algorithm in order to be stored for later use.
     */
    public static void initNodeData() {
        longitudes = Nodes.getLongitude();
        latitudes = Nodes.getLatitude();
        levels = Nodes.getLevels();

        //save heap space
        Nodes.setLongitude(null);
        Nodes.setLatitude(null);
        Nodes.setLevels(null);
    }

    /**
     * Initializes data related to edges used by the routing algorithm (specifically the part based on contraction
     * hierarchies) in order to be stored for later use.
     */
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
            if(nodeLvl >= hlLevel) { //for these nodes, we store labels, not edges
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

        //one more element, as final element makes it easier to iterate over the edges of the last node
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
        edgesOffset[nodeCount] = nextOffset; //initialize final element
     }

    /**
     * Initializes data related to labels used by the hub label algorithm in order to be stored for later use.
     */
    public static void initLabelInfo() throws IOException {
        int[][] tmpLabelNodes = Labels.getLabelNodes();
        int[][] tmpLabelEdges = Labels.getLabelEdges();
        int[][] tmpLabelDist = Labels.getLabelDist();
        //remove pointers so garbage collector can free this memory later on
        Labels.setLabelNodes(null);
        Labels.setLabelEdges(null);
        Labels.setLabelDist(null);
        String filePath = "tmp_HL_file";
        File f = new File(filePath);
        f.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));

        int nodeCount = tmpLabelNodes.length;
        //similarly to the edges offset array, add one additional element to simplify iteration over the last node
        labelOffset = new int[nodeCount + 1];
        int nextOffset = 0;
        //first write to file. This is necessary as otherwise too much RAM is used
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
        labelOffset[nodeCount] = nextOffset;
        writer.close();
        //free up memory
        tmpLabelNodes = null;
        tmpLabelEdges = null;
        tmpLabelDist = null;

        //read back from temp file into new format
        labelNode = new int[nextOffset + 1];
        labelEdge = new int[nextOffset + 1];
        labelDist = new int[nextOffset + 1];
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        for (int i = 0; i < nextOffset; i++) { //nextOffset is equal to the label count now
            labelNode[i] = Integer.parseInt(reader.readLine());
            labelEdge[i] = Integer.parseInt(reader.readLine());
            labelDist[i] = Integer.parseInt(reader.readLine());
        }
        //one additional element in the arrays. The reason for this is that the routing algorithm may overshoot by one
        //element in some cases in order to improve runtime.
        labelNode[nextOffset] = -1;
        labelEdge[nextOffset] = -1;
        labelDist[nextOffset] = -1;
        reader.close();
    }

    //simple getters and setters for fields. Used when serializing or deserializing data.

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
