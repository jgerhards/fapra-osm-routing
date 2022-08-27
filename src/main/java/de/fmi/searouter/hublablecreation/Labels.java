package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.OrderedIntSet;

public class Labels {
    private static OrderedIntSet[] labelNodes;
    private static OrderedIntSet[] labelEdges;
    private static OrderedIntSet[] labelDist;

    public static void initialize(int nodeCount) {
        labelNodes = new OrderedIntSet[nodeCount];
        labelEdges = new OrderedIntSet[nodeCount];
        labelDist = new OrderedIntSet[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            labelNodes[i] = new OrderedIntSet(false, 25, 25);
            labelEdges[i] = new OrderedIntSet(false, 25, 25);
            labelDist[i] = new OrderedIntSet(false, 25, 25);
        }
    }

    public static void addLabel(int nodeId, int labelId, int edgeId, int dist) {
        labelNodes[nodeId].insertTail(labelId);
        labelEdges[nodeId].insertTail(edgeId);
        labelDist[nodeId].insertTail(dist);
    }

    public static void setLabelNodes(OrderedIntSet[] labelNodes) {
        Labels.labelNodes = labelNodes;
    }

    public static void setLabelEdges(OrderedIntSet[] labelEdges) {
        Labels.labelEdges = labelEdges;
    }

    public static void setLabelDist(OrderedIntSet[] labelDist) {
        Labels.labelDist = labelDist;
    }

    public static OrderedIntSet[] getLabelNodes() {
        return labelNodes;
    }

    public static OrderedIntSet[] getLabelEdges() {
        return labelEdges;
    }

    public static OrderedIntSet[] getLabelDist() {
        return labelDist;
    }
}
