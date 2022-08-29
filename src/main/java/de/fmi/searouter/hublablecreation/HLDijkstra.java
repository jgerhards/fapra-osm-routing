package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.OrderedBoolSet;
import de.fmi.searouter.utils.OrderedIntSet;
import org.springframework.core.annotation.Order;

public class HLDijkstra extends Thread{
    private final int startNodeIdx;
    private final int endNodeIdx;
    private final CHDijkstraHeap heap; //we can reuse this class here
    private final OrderedIntSet foundIds;
    private final OrderedBoolSet idDistanceFinal;
    private final OrderedIntSet distances;
    private final OrderedIntSet firstEdgeId;
    private final int[] calcOrder;

    public HLDijkstra(int startNodeIdx, int endNodeIdx, int[] calcOrder) {
        this.startNodeIdx = startNodeIdx;
        this.endNodeIdx = endNodeIdx;
        this.calcOrder = calcOrder;
        heap = new CHDijkstraHeap();
        foundIds = new OrderedIntSet(true, 1000, 2000);
        distances = new OrderedIntSet(false, 1000, 2000);
        firstEdgeId = new OrderedIntSet(false, 1000, 2000);
        idDistanceFinal = new OrderedBoolSet(1000, 2000);
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

    private void writeLabels(int nodeId) {
        removeRedundancies(nodeId);
        //System.out.println("ttt: labelcount: " + foundIds.size());
        Labels.addLabels(nodeId, foundIds.toArray(), firstEdgeId.toArray(), distances.toArray());
    }

    private void removeRedundancies(int nodeId) {
        int idx = 0;
        OrderedIntSet labels = foundIds;
        while(idx < labels.size()) {
            int labelNode = labels.get(idx);
            if(labelNode != nodeId && Labels.isRedundant(labels.get(idx), distances.get(idx), foundIds, distances)) {
                labels.removeAtIdx(idx);
                distances.removeAtIdx(idx);
                firstEdgeId.removeAtIdx(idx);
            } else {
                idx++;
            }
        }
    }

    @Override
    public void run() {
        for (int nodeIdx = startNodeIdx; nodeIdx < endNodeIdx; nodeIdx++) {
            //System.out.println("ttt: current thread state: " + (nodeIdx - startNodeIdx) + ", " + (endNodeIdx - startNodeIdx));
            calcLabels(calcOrder[nodeIdx]);
            //System.out.println("ttt: len: " + Labels.getLabelNodes()[calcOrder[nodeIdx]].length);
        }
    }
}
