package de.fmi.searouter.hublablecreation;

import java.util.Arrays;

public class CHDijkstra {
    private static final int INITIAL_ARRAY_SIZE = 10;
    private static final int SIZE_INCREASE = 5;
    private static final int MULTIPLE_STEP_NUMBER = 10;

    private int[] foundIds;
    private int[] previousNodes;
    private int[] previousEdges;
    private int[] distances;
    private int foundIdCount;
    private CHDijkstraHeap heap;

    public CHDijkstra(int initialNode, int[] neighbours) {
        foundIds = new int[INITIAL_ARRAY_SIZE];
        distances = new int[INITIAL_ARRAY_SIZE];
        previousNodes = new int[INITIAL_ARRAY_SIZE];
        previousEdges = new int[INITIAL_ARRAY_SIZE];
        heap = new CHDijkstraHeap();
        foundIdCount = 0;

        calculateNew(initialNode, neighbours);
    }

    public void calculateNew(int initialNode, int[] neighbours) {
        Arrays.fill(foundIds, Integer.MAX_VALUE); //make sure binary search works

        foundIds[0] = initialNode;
        distances[0] = 0;
        foundIdCount = 0;

        while(!allNodesFound(neighbours) && !heap.isEmpty()) {
            multipleNextSteps();
        }
    }


    //todo:add function to get shortcuts via a given node


    private void multipleNextSteps() {
        for (int i = 0; i < MULTIPLE_STEP_NUMBER; i++) {
            if(!nextStep()) { //next step calculated in this call
                break;
            }
        }
    }

    private boolean nextStep() {
        if(heap.isEmpty()) {
            return false;
        }

        int nodeId = heap.getNext();

        int edgeCount = DynamicGrid.getCurrentEdgeCount(nodeId);
        int[] edgeIds = DynamicGrid.getCurrentEdges(nodeId);
        for (int i = 0; i < edgeCount; i++) {
            int edgeId = edgeIds[i];
            int destNode = Edges.getDest(edgeId);

            int destNodeIdx = addNodeIfNecessary(destNode);

            // Calculate the distance to the destination node using the current edge
            int newDistanceOverThisEdgeToDestVertex = distances[nodeId] + Edges.getDist(edgeId);

            // If the new calculated distance to the destination node is lower as the previously known
            // update the corresponding data structures
            if (newDistanceOverThisEdgeToDestVertex < distances[destNodeIdx]) {
                distances[destNodeIdx] = newDistanceOverThisEdgeToDestVertex;
                previousNodes[destNodeIdx] = nodeId;
                previousEdges[destNodeIdx] = edgeId;
                heap.add(destNode, newDistanceOverThisEdgeToDestVertex);
            }

        }

        return true;
    }

    private int addNodeIfNecessary(int nodeId) {
        int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
        if(nodeIdx < 0) {
            //node has to be added
            if(foundIds.length == foundIdCount) {
                grow();
            }
            nodeIdx = (nodeIdx + 1) * (-1);
            System.arraycopy(foundIds, nodeIdx, foundIds, nodeIdx + 1, foundIdCount - nodeIdx);
            foundIds[nodeIdx] = nodeId;
            System.arraycopy(distances, nodeIdx, distances, nodeIdx + 1, foundIdCount - nodeIdx);
            distances[nodeIdx] = Integer.MAX_VALUE;
            System.arraycopy(previousNodes, nodeIdx, previousNodes, nodeIdx + 1, foundIdCount - nodeIdx);
            System.arraycopy(previousEdges, nodeIdx, previousEdges, nodeIdx + 1, foundIdCount - nodeIdx);
            foundIdCount++;
        }
        return nodeIdx;
    }

    private void grow() {
        int oldLen = foundIds.length;
        foundIds = Arrays.copyOf(foundIds, oldLen + SIZE_INCREASE);
        distances = Arrays.copyOf(distances, oldLen + SIZE_INCREASE);
        previousNodes = Arrays.copyOf(previousNodes, oldLen + SIZE_INCREASE);
        previousEdges = Arrays.copyOf(previousEdges, oldLen + SIZE_INCREASE);
    }

    private boolean allNodesFound(int[] nodes) {
        for (int id : nodes) {
            if(Arrays.binarySearch(foundIds, id) < 0) {
                return false;
            }
        }
        return true;
    }
}
