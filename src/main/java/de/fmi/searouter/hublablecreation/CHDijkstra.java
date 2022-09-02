package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.IntArrayList;

import java.util.Arrays;

public class CHDijkstra extends Thread{
    private static final int INITIAL_ARRAY_SIZE = 10;
    private static final int SIZE_INCREASE = 5;
    private static final int MULTIPLE_STEP_NUMBER = 10;

    private int[] foundIds;
    private int[] previousNodes;
    private int[] previousEdges;
    private int[] distances;
    private boolean[] finalDistance;
    private int[] neighbours;
    // format: destId, distance, edgeId1, edgeId2 --> next (meaning three fields per shortcut)
    private final IntArrayList shortcuts;
    private int initialNode;
    private final IntArrayList intermediateNodes;
    private int foundIdCount;
    private final CHDijkstraHeap heap;

    public CHDijkstra() {
        foundIds = new int[INITIAL_ARRAY_SIZE];
        distances = new int[INITIAL_ARRAY_SIZE];
        finalDistance = new boolean[INITIAL_ARRAY_SIZE];
        previousNodes = new int[INITIAL_ARRAY_SIZE];
        previousEdges = new int[INITIAL_ARRAY_SIZE];
        shortcuts = new IntArrayList(INITIAL_ARRAY_SIZE * 4);
        heap = new CHDijkstraHeap();
        foundIdCount = 0;
        intermediateNodes = new IntArrayList(5000);
    }

    public void addNodeId(int id) {
        intermediateNodes.add(id);
    }

    public void run() {
        System.out.println("ttt: run called");
        shortcuts.clear();
        int nodeNum = intermediateNodes.getLen();
        for (int nodeIdx = 0; nodeIdx < nodeNum; nodeIdx++) {
            int currentNode = intermediateNodes.get(nodeIdx);
            if(currentNode == 5) {
                System.out.println("a 12");
            }
            //System.out.println("ttt:-------------------------------------------------------- " + currentNode);
            //get all relevant edge ids
            int edgeCount = DynamicGrid.getCurrentEdgeCount(currentNode);
            if(edgeCount < 2) {
                //no need to check further, no shortcut can possibly be made
                continue;
            }
            int[] edges = DynamicGrid.getCurrentEdges(currentNode);

            //get all neighbor node ids
            int[] neighbours = new int[edgeCount];
            for (int i = 0; i < edgeCount; i++) {
                neighbours[i] = Edges.getDest(edges[i]);
            }

            for (int neighbourId : neighbours) {
                nextCalc(neighbourId, neighbours, currentNode);
                addShortcuts(currentNode);
            }
        }
        intermediateNodes.clear();
    }

    public IntArrayList getShortcuts() {
        return shortcuts;
    }

    private void nextCalc(int initialNode, int[] neighbours, int preferredNode) {
        if(initialNode == 50162) {
            System.out.println("a8");
        }
        this.neighbours = neighbours;
        this.initialNode = initialNode;
        Arrays.fill(foundIds, Integer.MAX_VALUE); //make sure binary search works
        Arrays.fill(distances, 0);
        heap.reset();

        foundIds[0] = initialNode;
        distances[0] = 0;
        foundIdCount = 1;
        heap.add(initialNode, 0);

        while(!allNodesFound(neighbours) && !heap.isEmpty()) {
            if(initialNode == 50162) {
                System.out.println("a9");
            }
            multipleNextSteps(preferredNode);
        }
    }

    private void addShortcuts(int nodeId) {
        int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
        if(nodeIdx < 0 ){
            System.out.println("ttt: nodeId: " + nodeId + " " + initialNode);
        }
        if(previousNodes[nodeIdx] != initialNode) {
            //in this case, no shortcut from the initial node
            return;
        }
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
                shortcuts.add(initialNode);
                shortcuts.add(neighbourId);
                shortcuts.add(distances[neighbourIdx]);
                shortcuts.add(previousEdges[neighbourIdx]);
                shortcuts.add(previousEdges[nodeIdx]);
            }
        }
    }

    private void multipleNextSteps(int preferredNode) {
        for (int i = 0; i < MULTIPLE_STEP_NUMBER; i++) {
            if(!nextStep(preferredNode)) { //next step calculated in this call
                break;
            }
        }
    }

    private boolean nextStep(int preferredNode) {
        if(heap.isEmpty()) {
            return false;
        }

        int nodeId = heap.getNext();

        /*if(nodeId == 3092) {
            System.out.println("a3");
        }*/

        int edgeCount = DynamicGrid.getCurrentEdgeCount(nodeId);
        //System.out.println("ttt: current edges count: " + edgeCount);
        int[] edgeIds = DynamicGrid.getCurrentEdges(nodeId);
        for (int i = 0; i < edgeCount; i++) {
            int edgeId = edgeIds[i];
            int destNode = Edges.getDest(edgeId);

            int destNodeIdx = addNodeIfNecessary(destNode);

            // Calculate the distance to the destination node using the current edge
            int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
            finalDistance[nodeIdx] = true;
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
            } else if (nodeId == preferredNode && newDistanceOverThisEdgeToDestVertex == distances[destNodeIdx]) {
                //in this case we prefer the way over the note to be contracted
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
            System.arraycopy(finalDistance, nodeIdx, finalDistance, nodeIdx + 1, foundIdCount - nodeIdx);
            finalDistance[nodeIdx] = false;
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
        finalDistance = Arrays.copyOf(finalDistance, oldLen + SIZE_INCREASE);
        previousNodes = Arrays.copyOf(previousNodes, oldLen + SIZE_INCREASE);
        previousEdges = Arrays.copyOf(previousEdges, oldLen + SIZE_INCREASE);
    }

    private boolean allNodesFound(int[] nodes) {
        for (int id : nodes) {
            int idx = Arrays.binarySearch(foundIds, id);
            if(idx < 0) {
                return false;
            } else if(!finalDistance[idx]) {
                return false;
            }
        }
        return true;
    }
}
