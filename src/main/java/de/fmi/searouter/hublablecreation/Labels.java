package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.OrderedIntSet;

public class Labels {
    private static int[][] labelNodes;
    private static int[][] labelEdges;
    private static int[][] labelDist;

    public static void initialize(int nodeCount) {
        labelNodes = new int[nodeCount][];
        labelEdges = new int[nodeCount][];
        labelDist = new int[nodeCount][];
    }

    public static void addLabels(int nodeId, int[] labelIds, int[] edgeIds, int[] dists) {
        labelNodes[nodeId] = labelIds;
        labelEdges[nodeId] = edgeIds;
        labelDist[nodeId] = dists;
    }

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
                if(newDist <= checkDist) {
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
