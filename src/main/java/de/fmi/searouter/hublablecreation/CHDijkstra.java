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
    private int[] neighbours;
    // format: destId, distance, edgeId1, edgeId2 --> next (meaning three fields per shortcut)
    private int[] shortcuts;
    private int initialNode;
    private int foundIdCount;
    private CHDijkstraHeap heap;

    public CHDijkstra() {
        foundIds = new int[INITIAL_ARRAY_SIZE];
        distances = new int[INITIAL_ARRAY_SIZE];
        previousNodes = new int[INITIAL_ARRAY_SIZE];
        previousEdges = new int[INITIAL_ARRAY_SIZE];
        shortcuts = new int[INITIAL_ARRAY_SIZE * 4];
        heap = new CHDijkstraHeap();
        foundIdCount = 0;
    }

    public void calculateNew(int initialNode, int[] neighbours) {
        if(initialNode == 647860) {
            System.out.println("a4");
        }
        Arrays.fill(foundIds, Integer.MAX_VALUE); //make sure binary search works
        Arrays.fill(distances, 0);
        heap.reset();

        //store information for later
        this.neighbours = neighbours;
        this.initialNode = initialNode;

        foundIds[0] = initialNode;
        distances[0] = 0;
        foundIdCount = 1;
        heap.add(initialNode, 0);

        while(!allNodesFound(neighbours) && !heap.isEmpty()) {
            multipleNextSteps();
        }
    }

    public int findShortcuts(int nodeId) {
        int numOfShortcuts = 0;
        int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
        for (int neighbourId : neighbours) {
            int neighbourIdx = Arrays.binarySearch(foundIds, neighbourId);
            if(neighbourId == initialNode) {
                continue;
            }
            if(neighbourIdx < 0) {
                System.out.println("ttt: stelle 1: " + allNodesFound(neighbours)); //todo: here
            }
            if(previousNodes[neighbourIdx] == nodeId) {
                // this means a shortcut exists. This consists of exactly two edges (X-->nodeId-->Y)
                int nextShortcutIdx = (numOfShortcuts * 4);
                numOfShortcuts++;
                if(numOfShortcuts * 4 >= shortcuts.length) {
                    growShortcutArray();
                }
                shortcuts[nextShortcutIdx] = neighbourId;
                shortcuts[nextShortcutIdx + 1] = distances[neighbourIdx];
                shortcuts[nextShortcutIdx + 2] = previousEdges[neighbourIdx];
                shortcuts[nextShortcutIdx + 3] = previousEdges[nodeIdx];
            }
        }

        return numOfShortcuts;
    }

    public int[] getShortcuts() {
        return shortcuts;
    }

    private void growShortcutArray() {
        int oldLen = shortcuts.length;
        shortcuts = Arrays.copyOf(shortcuts, oldLen + (SIZE_INCREASE * 4));
    }

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

        if(nodeId == 3092) {
            System.out.println("a3");
        }

        int edgeCount = DynamicGrid.getCurrentEdgeCount(nodeId);
        //System.out.println("ttt: current edges count: " + edgeCount);
        int[] edgeIds = DynamicGrid.getCurrentEdges(nodeId);
        for (int i = 0; i < edgeCount; i++) {
            int edgeId = edgeIds[i];
            int destNode = Edges.getDest(edgeId);

            int destNodeIdx = addNodeIfNecessary(destNode);

            // Calculate the distance to the destination node using the current edge
            int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
            int newDistanceOverThisEdgeToDestVertex = distances[nodeIdx] + Edges.getDist(edgeId);
            if(newDistanceOverThisEdgeToDestVertex == -1) {
                System.out.println("a1");
            }

            // If the new calculated distance to the destination node is lower as the previously known
            // update the corresponding data structures
            if (distances[destNodeIdx] == -1 || newDistanceOverThisEdgeToDestVertex < distances[destNodeIdx]) {
                distances[destNodeIdx] = newDistanceOverThisEdgeToDestVertex;
                previousNodes[destNodeIdx] = nodeId;
                previousEdges[destNodeIdx] = edgeId;
                heap.add(destNode, newDistanceOverThisEdgeToDestVertex);
                //System.out.println("ttt: heap adds node " + destNode); todo: remove
            }

        }

        return true;
    }

    private int addNodeIfNecessary(int nodeId) {
        int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
        if(nodeIdx < 0) {
            //System.out.println("ttt: add node " + nodeId); todo: remove
            //node has to be added
            if(foundIds.length == foundIdCount) {
                grow();
            }
            nodeIdx = (nodeIdx + 1) * (-1);
            if(foundIdCount - nodeIdx < 0) {
                System.out.println("a2");
            }
            System.arraycopy(foundIds, nodeIdx, foundIds, nodeIdx + 1, foundIdCount - nodeIdx);
            foundIds[nodeIdx] = nodeId;
            System.arraycopy(distances, nodeIdx, distances, nodeIdx + 1, foundIdCount - nodeIdx);
            distances[nodeIdx] = -1;
            System.arraycopy(previousNodes, nodeIdx, previousNodes, nodeIdx + 1, foundIdCount - nodeIdx);
            System.arraycopy(previousEdges, nodeIdx, previousEdges, nodeIdx + 1, foundIdCount - nodeIdx);
            foundIdCount++;
        }
        return nodeIdx;
    }

    private void grow() {
        int oldLen = foundIds.length;
        foundIds = Arrays.copyOf(foundIds, oldLen + SIZE_INCREASE);
        Arrays.fill(foundIds, oldLen, foundIds.length, Integer.MAX_VALUE);
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
