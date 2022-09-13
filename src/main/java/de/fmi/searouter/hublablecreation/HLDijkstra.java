package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.DistanceHeap;
import de.fmi.searouter.utils.OrderedBoolSet;
import de.fmi.searouter.utils.OrderedIntSet;

/**
 * Thread used to generate hub labels based on contraction hierarchy.
 */
public class HLDijkstra extends Thread{
    //the order in which nodes are to be calculated
    private final int[] calcOrder;
    //first index whose node id should be calculated by this thread and first index whose should not
    private final int startNodeIdx;
    private final int endNodeIdx;

    //list of ids found so far. if an id is found at a certain index in this list, associated data can be found in other
    //lists at the same index.
    private final OrderedIntSet foundIds;
    //distances from the initial node to other nodes
    private final OrderedIntSet distances;
    //first edge used when navigating from the initial node to a different one
    private final OrderedIntSet firstEdgeId;

    /**
     * Constructor. Used to pass initial data about this thread.
     * @param startNodeIdx the index at which to start (in calc order)
     * @param endNodeIdx the index at which to stop (in calc order)
     * @param calcOrder an array containing ids of nodes in the order in which they should be calculated in
     */
    public HLDijkstra(int startNodeIdx, int endNodeIdx, int[] calcOrder) {
        this.startNodeIdx = startNodeIdx;
        this.endNodeIdx = endNodeIdx;
        this.calcOrder = calcOrder;
        foundIds = new OrderedIntSet(true, 1000, 2000);
        distances = new OrderedIntSet(false, 1000, 2000);
        firstEdgeId = new OrderedIntSet(false, 1000, 2000);
    }

    /**
     * Calculate the labels for a given node.
     * @param nodeId the id of the node
     */
    private void calcLabels(int nodeId) {
        foundIds.clear();
        distances.clear();
        firstEdgeId.clear();

        foundIds.insertSorted(nodeId);
        distances.insertTail(0);
        firstEdgeId.insertTail(-1);

        int edgeCount = DynamicGrid.getAllEdgeCount(nodeId);
        int[] edgeIds = DynamicGrid.getAllEdges(nodeId);
        int nodeLvl = Nodes.getNodeLvl(nodeId);
        int[] neighbourIds = new int[edgeCount]; //list of direct neighbours, relevant later
        int realNeighbourCount = 0;
        //look at all edges as only the ones to higher lvl nodes are stored
        for (int i = 0; i < edgeCount; i++) {
            int edgeId = edgeIds[i];
            int destNode = Edges.getDest(edgeId);
            if(Nodes.getNodeLvl(destNode) < nodeLvl) { //ignore lower lvl nodes
                continue;
            }
            int edgeDist = Edges.getDist(edgeId);
            neighbourIds[realNeighbourCount] = destNode;
            realNeighbourCount++;

            int insertIdx = foundIds.getIdx(destNode);
            if(insertIdx < 0) {
                insertIdx = (insertIdx + 1) * (-1);
                foundIds.insertAtIdx(destNode, insertIdx);
                distances.insertAtIdx(edgeDist, insertIdx);
                firstEdgeId.insertAtIdx(edgeId, insertIdx);
            } else {
                System.out.println("error: this code should not be reached");
            }
        }

        for(int j = 0; j < realNeighbourCount; j++) {
            int baseNodeId = neighbourIds[j];
            int baseIdx = foundIds.getIdx(baseNodeId);
            int baseDist = distances.get(baseIdx);
            int baseEdge = firstEdgeId.get(baseIdx);

            int[] labels = Labels.getLabels(baseNodeId);
            int labelCount = labels.length;
            int[] dist = Labels.getDist(baseNodeId);

            for (int i = 0; i < labelCount; i++) {
                int currLabelNode = labels[i];
                int currLabelIdx = foundIds.getIdx(currLabelNode);

                if(currLabelIdx < 0) {
                    //new node found
                    currLabelIdx = (currLabelIdx + 1) * (-1);
                    int distance = baseDist + dist[i];
                    foundIds.insertAtIdx(currLabelNode, currLabelIdx);
                    distances.insertAtIdx(distance, currLabelIdx);
                    firstEdgeId.insertAtIdx(baseEdge, currLabelIdx);
                } else {
                    int newDistance = baseDist + dist[i];
                    int oldDistance = distances.get(currLabelIdx);
                    if(oldDistance > newDistance) {
                        distances.updateValue(newDistance, currLabelIdx);
                        firstEdgeId.updateValue(baseEdge, currLabelIdx);
                    }
                }
            }
        }

        addLabels(nodeId);
    }

    /**
     * Add labels to the {@link Labels} data structure. Before doing that, remove any redundant labels.
     * @param nodeId the id of the node to add the labels for
     */
    private void addLabels(int nodeId) {
        removeRedundancies(nodeId);
        Labels.addLabels(nodeId, foundIds.toArray(), firstEdgeId.toArray(), distances.toArray());
    }

    /**
     * Remove redundant labels of a given node. A label is redundant, if the label node has another label in
     * common with the given node, where the distance is smaller than or equal to the distance of the
     * original label.
     * @param nodeId the id of the node to check the labels of
     */
    private void removeRedundancies(int nodeId) {
        int idx = 0;
        OrderedIntSet labels = foundIds;
        while(idx < labels.size()) {
            int labelNode = labels.get(idx);
            if(labelNode != nodeId && Labels.isRedundant(labels.get(idx), distances.get(idx), foundIds, distances)) {
                labels.removeAtIdx(idx);
                distances.removeAtIdx(idx);
                firstEdgeId.removeAtIdx(idx);
                //no need to increase idx, as an element was removed
            } else {
                idx++;
            }
        }
    }

    /**
     * Calculate labels for node ids in the calc order array between the indices given for this thread.
     */
    @Override
    public void run() {
        for (int nodeIdx = startNodeIdx; nodeIdx < endNodeIdx; nodeIdx++) {
            calcLabels(calcOrder[nodeIdx]);
        }
    }
}
