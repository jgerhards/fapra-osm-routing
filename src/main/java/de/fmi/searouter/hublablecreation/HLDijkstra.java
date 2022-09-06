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

    //heap used during dijkstra part of calculation
    private final DistanceHeap heap;

    //list of ids found so far. if an id is found at a certain index in this list, associated data can be found in other
    //lists at the same index.
    private final OrderedIntSet foundIds;
    //tracks if the distance to an id is final
    private final OrderedBoolSet idDistanceFinal;
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
        heap = new DistanceHeap();
        foundIds = new OrderedIntSet(true, 1000, 2000);
        distances = new OrderedIntSet(false, 1000, 2000);
        firstEdgeId = new OrderedIntSet(false, 1000, 2000);
        idDistanceFinal = new OrderedBoolSet(1000, 2000);
    }

    /**
     * Calculate the labels for a given node.
     * @param nodeId the id of the node
     */
    private void calcLabels(int nodeId) {
        foundIds.clear();
        idDistanceFinal.clear();
        distances.clear();
        firstEdgeId.clear();
        heap.reset();

        foundIds.insertSorted(nodeId);
        idDistanceFinal.insertTail(true);
        distances.insertTail(0);
        firstEdgeId.insertTail(-1);
        heap.add(nodeId, 0);
        while(!heap.isEmpty()) {
            int currNode = heap.getNext();
            int currNodeIdx = foundIds.getIdx(currNode);
            idDistanceFinal.updateValue(true, currNodeIdx); //once popped from the heap, the distance is final
            int edgeCount = DynamicGrid.getAllEdgeCount(currNode);
            int[] edgeIds = DynamicGrid.getAllEdges(currNode);
            int nodeLvl = Nodes.getNodeLvl(currNode);
            int currDistance = distances.get(foundIds.getIdx(currNode));

            for (int i = 0; i < edgeCount; i++) { //consider all edges of the current node
                int edgeId = edgeIds[i];
                int destNode = Edges.getDest(edgeId);
                if(nodeLvl > Nodes.getNodeLvl(destNode)) { //ignore lower lvl nodes
                    continue;
                }

                int destNodeIdx = foundIds.getIdx(destNode);
                if(destNodeIdx >= 0) { // contained in foundIds
                    if(idDistanceFinal.get(destNodeIdx)) {
                        continue;
                    } else {
                        int oldDistance = distances.get(destNodeIdx);
                        int newDistance = currDistance + Edges.getDist(edgeId);
                        if(oldDistance > newDistance) {
                            //update to new values
                            distances.updateValue(newDistance, destNodeIdx);
                            firstEdgeId.updateValue(firstEdgeId.get(currNodeIdx), destNodeIdx);
                            heap.add(destNode, newDistance);
                        }
                    }
                } else { //not contained in foundIds --> new node found
                    destNodeIdx = foundIds.insertSorted(destNode);
                    int distance = currDistance + Edges.getDist(edgeId);
                    distances.insertAtIdx(distance, destNodeIdx);
                    idDistanceFinal.insertAtIdx(false, destNodeIdx);
                    if(currNode == nodeId) {
                        //if this is true, we look at the initial node --> first edge id is the current edge
                        firstEdgeId.insertAtIdx(edgeId, destNodeIdx);
                    } else {
                        //use old currNodeIdx on purpose here since nothing has been inserted into this structure so far
                        firstEdgeId.insertAtIdx(firstEdgeId.get(currNodeIdx), destNodeIdx);
                    }
                    //currNodeIdx has to be updated as we added an element --> this may have moved currNode
                    currNodeIdx = foundIds.getIdx(currNode);
                    heap.add(destNode, distance);
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
