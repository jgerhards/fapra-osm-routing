package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.OrderedBoolSet;
import de.fmi.searouter.utils.OrderedIntSet;

public class HLDijkstra extends Thread{
    private final int startNodeId;
    private final int endNodeId;
    private final CHDijkstraHeap heap; //we can reuse this class here
    private final OrderedIntSet foundIds;
    private final OrderedBoolSet idDistanceFinal;
    private final OrderedIntSet distances;
    private final OrderedIntSet firstEdgeId;

    public HLDijkstra(int startNodeId, int endNodeId) {
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        heap = new CHDijkstraHeap();
        foundIds = new OrderedIntSet(true, 30, 20);
        distances = new OrderedIntSet(false, 30, 20);
        firstEdgeId = new OrderedIntSet(false, 30, 20);
        idDistanceFinal = new OrderedBoolSet(30, 20);
    }

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
            idDistanceFinal.updateValue(true, currNodeIdx);
            int edgeCount = DynamicGrid.getAllEdgeCount(currNode);
            int[] edgeIds = DynamicGrid.getAllEdges(currNode);
            int nodeLvl = Nodes.getNodeLvl(currNode);
            int currDistance = distances.get(foundIds.getIdx(currNode));

            for (int i = 0; i < edgeCount; i++) {
                int edgeId = edgeIds[i];
                int destNode = Edges.getDest(edgeId);
                if(nodeLvl > Nodes.getNodeLvl(destNode)) {
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
                            distances.updateValue(newDistance, destNodeIdx);
                            firstEdgeId.updateValue(firstEdgeId.get(currNodeIdx), destNodeIdx);
                            heap.add(destNode, newDistance);
                        }
                    }
                } else {
                    destNodeIdx = foundIds.insertSorted(destNode);
                    int distance = currDistance + Edges.getDist(edgeId);
                    distances.insertAtIdx(distance, destNodeIdx);
                    idDistanceFinal.insertAtIdx(false, destNodeIdx);
                    if(currNode == nodeId) {
                        firstEdgeId.insertAtIdx(edgeId, destNodeIdx);
                    } else {
                        firstEdgeId.insertAtIdx(firstEdgeId.get(currNodeIdx), destNodeIdx);
                    }
                    heap.add(destNode, distance);
                }
            }
        }
        writeLabels(nodeId);
    }

    public void writeLabels(int nodeId) {
        int labelCount = foundIds.size();
        for (int i = 0; i < labelCount; i++) {
            if(!idDistanceFinal.get(i)) {
                System.out.println("ttt: error");
                System.exit(-1);
            }
            Labels.addLabel(nodeId, foundIds.get(i), firstEdgeId.get(i), distances.get(i));
        }
    }

    @Override
    public void run() {
        for (int nodeId = startNodeId; nodeId < endNodeId; nodeId++) {
            System.out.println("ttt: current thread state: " + (nodeId - startNodeId) + ", " + (endNodeId-startNodeId));
            calcLabels(nodeId);
            System.out.println("ttt: len: " + Labels.getLabelNodes()[nodeId].size());
        }
    }
}
