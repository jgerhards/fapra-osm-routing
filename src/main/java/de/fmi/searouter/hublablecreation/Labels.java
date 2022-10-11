package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.OrderedIntSet;

/**
 * This class contains data used during calculation of labels. Its task is to store and process previously
 * calculated labels.
 */
public class Labels {
    //the nodes of the labels, ordered by id of nodes and id of labels
    private static int[][] labelNodes;
    //the first edge of the labels, ordered by id of nodes and order in labelNodes
    private static int[][] labelEdges;
    //the distance to the nodes of labels, ordered by id of nodes and order in labelNodes
    private static int[][] labelDist;

    /**
     * Initialize data structures to store data. Keep in mind that the second dimension of arrays
     * is still null even after this call.
     * @param nodeCount the number of nodes
     */
    public static void initialize(int nodeCount) {
        labelNodes = new int[nodeCount][];
        labelEdges = new int[nodeCount][];
        labelDist = new int[nodeCount][];
    }

    /**
     * Get labels for a node with a given id.
     * @param nodeId the id of the node
     * @return the array of labels
     */
    public static int[] getLabels(int nodeId) {
        return labelNodes[nodeId];
    }

    /**
     * Get distances array for a node with a given id.
     * @param nodeId the id of the node
     * @return the array of distances
     */
    public static int[] getDist(int nodeId) {
        return labelDist[nodeId];
    }

    /**
     * Add labels for a node with a given id. This includes all data relevant for the label.
     * @param nodeId id of the node the labels are for
     * @param labelIds an array of node ids of labels
     * @param edgeIds an array of edge ids of labels
     * @param dists an array of distances of labels
     */
    public static void addLabels(int nodeId, int[] labelIds, int[] edgeIds, int[] dists) {
        labelNodes[nodeId] = labelIds;
        labelEdges[nodeId] = edgeIds;
        labelDist[nodeId] = dists;
    }

    /**
     * Check if a given label is redundant. A label is considered redundant, if the node in the label has
     * a label in common with the original node whose distance is smaller than or equal to
     * the one associated with the label to check.
     * @param toCheck the label to check
     * @param checkDist the distance of the label to check
     * @param labels the label nodes of the original node
     * @param checkDists the distances associated with the labels of the original nodes
     * @return true if the label is redundant, else false
     */
    public static boolean isRedundant(int toCheck, int checkDist, OrderedIntSet labels, OrderedIntSet checkDists) {
        int checkIdx = 0;
        int[] checkLabels = labelNodes[toCheck];
        int checkLabelCount = checkLabels.length;
        int labelIdx = 0;
        int labelCount = labels.size();
        while(checkIdx < checkLabelCount && labelIdx < labelCount) {
            int checkNode = checkLabels[checkIdx];
            int labelNode = labels.get(labelIdx);
            if(checkNode == labelNode) {
                int newDist = checkDists.get(labelIdx) + labelDist[toCheck][checkIdx];
                if(newDist < checkDist) {
                    if(!(checkNode == toCheck)) {
                        return true;
                    }
                }
                checkIdx++;
                labelIdx++;
            } else if(checkNode < labelNode) {
                checkIdx++;
            } else {
                labelIdx++;
            }
        }
        return false;
    }

    //simple getters and setters for fields. Used when serializing or deserializing data.

    public static void setLabelNodes(int[][] labelNodes) {
        Labels.labelNodes = labelNodes;
    }

    public static void setLabelEdges(int[][] labelEdges) {
        Labels.labelEdges = labelEdges;
    }

    public static void setLabelDist(int[][] labelDist) {
        Labels.labelDist = labelDist;
    }

    public static int[][] getLabelNodes() {
        return labelNodes;
    }

    public static int[][] getLabelEdges() {
        return labelEdges;
    }

    public static int[][] getLabelDist() {
        return labelDist;
    }
}
