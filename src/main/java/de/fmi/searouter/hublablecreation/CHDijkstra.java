package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.DistanceHeap;
import de.fmi.searouter.utils.IntArrayList;

import java.util.Arrays;

/**
 * Thread used to calculate contraction hierarchies using the dijkstra algorithm.
 */
public class CHDijkstra extends Thread{
    private static final int INITIAL_ARRAY_SIZE = 10;
    private static final int SIZE_INCREASE = 5;
    private static final int MULTIPLE_STEP_NUMBER = 10;

    //ordered list of ids found so far by dijkstra algorithm
    private int[] foundIds;
    //number of ids found
    private int foundIdCount;
    //previous node (according to dijkstra) for the node with the id at the same index in foundIds
    private int[] previousNodes;
    //previous edge (according to dijkstra) for the node with the id at the same index in foundIds
    private int[] previousEdges;
    //distance (according to dijkstra) from the initial node to the node with the id at the same index in foundIds
    private int[] distances;
    //if true at an index, the distance from the initial node to the node with id at the same index in foundIds is final
    private boolean[] finalDistance;

    //list of neighboring nodes which should be considered
    private int[] neighbours;
    //initial node to start dijkstra from
    private int initialNode;
    // format: startId, destId, distance, edgeId1, edgeId2 --> next (meaning four fields per shortcut)
    private final IntArrayList shortcuts;
    //heap used during dijkstra calculation
    private final DistanceHeap heap;

    //ids of nodes for which to calculate contraction in this thread
    private final IntArrayList nodeIdsToCalc;

    /**
     * Constructor. Initializes data structures according to initial sizes.
     */
    public CHDijkstra() {
        foundIds = new int[INITIAL_ARRAY_SIZE];
        distances = new int[INITIAL_ARRAY_SIZE];
        finalDistance = new boolean[INITIAL_ARRAY_SIZE];
        previousNodes = new int[INITIAL_ARRAY_SIZE];
        previousEdges = new int[INITIAL_ARRAY_SIZE];
        shortcuts = new IntArrayList(INITIAL_ARRAY_SIZE * 4);
        heap = new DistanceHeap();
        foundIdCount = 0;
        nodeIdsToCalc = new IntArrayList(5000);
    }

    /**
     * Adds a node (by id) to calculate a contraction for in this thread.
     * @param id the id of the node
     */
    public void addNodeId(int id) {
        nodeIdsToCalc.add(id);
    }

    /**
     * Contracts all nodes contained in nodeIdsToCalc.
     */
    public void run() {
        shortcuts.clear();
        int nodeNum = nodeIdsToCalc.getLen();
        for (int nodeIdx = 0; nodeIdx < nodeNum; nodeIdx++) {
            int currentNode = nodeIdsToCalc.get(nodeIdx);
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
        nodeIdsToCalc.clear();
    }

    /**
     * Get all shortcuts that were calculated in the thread. Format is startId, destId, distance, edgeId1, edgeId2.
     * Every shortcut consists of 4 elements.
     * @return the shortcuts
     */
    public IntArrayList getShortcuts() {
        return shortcuts;
    }

    /**
     * Calculate dijkstra for a node. The relevant results will be stored in the fields of the thread.
     * A preferred node can be given. In this case, routes will be preferred when containing this node.
     * This is only done if the distances of the different routes are identical.
     * @param initialNode the node to start the dijkstra from
     * @param neighbours the nodes which have to be found
     * @param preferredNode the preferred route
     */
    private void nextCalc(int initialNode, int[] neighbours, int preferredNode) {
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
            multipleNextSteps(preferredNode);
        }
    }

    /**
     * Calculate the shortcuts which traverse a given node and add them to the shortcuts list.
     * @param nodeId the node to route shortcuts over
     */
    private void addShortcuts(int nodeId) {
        int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
        if(previousNodes[nodeIdx] != initialNode) {
            //in this case, no shortcut from the initial node
            return;
        }
        for (int neighbourId : neighbours) {
            int neighbourIdx = Arrays.binarySearch(foundIds, neighbourId);
            if(neighbourId == initialNode) {
                continue;
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

    /**
     * calculate multiple steps in the dijkstra. This is done so the neighbours do not have to be checked every step.
     * @param preferredNode the preferred node
     */
    private void multipleNextSteps(int preferredNode) {
        for (int i = 0; i < MULTIPLE_STEP_NUMBER; i++) {
            if(!nextStep(preferredNode)) { //next step calculated in this call
                break;
            }
        }
    }

    /**
     * Calculate the next step in the dijkstra algorithm.
     * @param preferredNode the preferred node
     * @return false if the heap is empty, else true
     */
    private boolean nextStep(int preferredNode) {
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
            int nodeIdx = Arrays.binarySearch(foundIds, nodeId);
            finalDistance[nodeIdx] = true;
            int newDistanceOverThisEdgeToDestVertex = distances[nodeIdx] + Edges.getDist(edgeId);

            // If the new calculated distance to the destination node is lower as the previously known
            // update the corresponding data structures
            if (distances[destNodeIdx] == -1 || newDistanceOverThisEdgeToDestVertex < distances[destNodeIdx]) {
                distances[destNodeIdx] = newDistanceOverThisEdgeToDestVertex;
                previousNodes[destNodeIdx] = nodeId;
                previousEdges[destNodeIdx] = edgeId;
                heap.add(destNode, newDistanceOverThisEdgeToDestVertex);
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

    /**
     * Add the id of a node to the foundId array if it is not already contained. Also update other
     * data structures of the thread to reflect a change in indices if the id has to be added.
     * @param nodeId the id to add
     * @return the index at which the id is contained in foundIds
     */
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
            System.arraycopy(finalDistance, nodeIdx, finalDistance, nodeIdx + 1, foundIdCount - nodeIdx);
            finalDistance[nodeIdx] = false;
            distances[nodeIdx] = -1;
            System.arraycopy(previousNodes, nodeIdx, previousNodes, nodeIdx + 1, foundIdCount - nodeIdx);
            System.arraycopy(previousEdges, nodeIdx, previousEdges, nodeIdx + 1, foundIdCount - nodeIdx);
            foundIdCount++;
        }
        return nodeIdx;
    }

    /**
     * Increase the size of all data structures in this thread.
     */
    private void grow() {
        int oldLen = foundIds.length;
        foundIds = Arrays.copyOf(foundIds, oldLen + SIZE_INCREASE);
        Arrays.fill(foundIds, oldLen, foundIds.length, Integer.MAX_VALUE);
        distances = Arrays.copyOf(distances, oldLen + SIZE_INCREASE);
        finalDistance = Arrays.copyOf(finalDistance, oldLen + SIZE_INCREASE);
        previousNodes = Arrays.copyOf(previousNodes, oldLen + SIZE_INCREASE);
        previousEdges = Arrays.copyOf(previousEdges, oldLen + SIZE_INCREASE);
    }

    /**
     * Check if all given nodes are contained in the foundIds array.
     * @param nodes the nodes to check
     * @return true if they are contained, else false
     */
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
