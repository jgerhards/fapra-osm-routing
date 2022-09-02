package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.dijkstragrid.Node;
import de.fmi.searouter.router.DijkstraRouter;

import java.util.Arrays;

public class CHDijkstraHeap {
    private final int INITIAL_SIZE = 10;
    private final int SIZE_INCREASE = 5;

    //for each node, the position of it in the heap, based on the index
    private int[] heapPosition;
    //the array representing the heap
    private int[] idHeapArray;
    private int currentSize;
    //used when comparing the distances of ids
    private int[] containedIds;
    private int[] distances;

    public CHDijkstraHeap() {
        this.heapPosition = new int[INITIAL_SIZE];
        this.idHeapArray = new int[INITIAL_SIZE];
        this.containedIds = new int[INITIAL_SIZE];
        this.distances = new int[INITIAL_SIZE];
        currentSize = 0;
        Arrays.fill(containedIds, Integer.MAX_VALUE); //make sure binary search works
    }

    public void reset() {
        currentSize = 0;
        Arrays.fill(containedIds, Integer.MAX_VALUE); //make sure binary search works
    }

    /**
     * checks if the heap is empty
     * @return true if no more elements are contained on the heap, else false
     */
    public boolean isEmpty() {
        return (currentSize == 0);
    }

    /**
     * gets the id of the node with the lowest distance from the start node stored on the heap. Also restores
     * the remaining array to a heap.
     * @return the id of the node with the shortest distance
     */
    public int getNext() {
        int returnValue = idHeapArray[0];

        int deleteIdx = Arrays.binarySearch(containedIds, idHeapArray[0]);
        currentSize--;
        idHeapArray[0] = idHeapArray[currentSize];
        //get position in heap position array
        int positionIdx = Arrays.binarySearch(containedIds, idHeapArray[0]);
        heapPosition[positionIdx] = 0;
        heapifyTopDown(currentSize, 0);
        if(deleteIdx < currentSize) {
            System.arraycopy(containedIds, deleteIdx + 1, containedIds, deleteIdx, currentSize - deleteIdx);
            System.arraycopy(distances, deleteIdx + 1, distances, deleteIdx, currentSize - deleteIdx);
            System.arraycopy(heapPosition, deleteIdx + 1, heapPosition, deleteIdx, currentSize - deleteIdx);
        }
        containedIds[currentSize] = Integer.MAX_VALUE;
        return returnValue;
    }

    /**
     * adds an id to the heap. if the id is already on the heap, updates its position if necessary
     * (for example after an update).
     * @param id the id to add
     */
    public void add(int id, int distance) {
        int idx = Arrays.binarySearch(containedIds, id);
        if(idx >= 0) {
            distances[idx] = distance;
            //update, do not add again
            heapifyBottomUp(heapPosition[idx]);
        } else {
            if(currentSize == idHeapArray.length) {
                grow();
            }
            int insertIdx = (idx + 1) * (-1);
            /*if(currentSize == 3) {
                //todo: remove
                System.out.println("a5");
            }*/
            System.arraycopy(containedIds, insertIdx, containedIds, insertIdx + 1, currentSize - insertIdx);
            containedIds[insertIdx] = id;
            System.arraycopy(distances, insertIdx, distances, insertIdx + 1, currentSize - insertIdx);
            distances[insertIdx] = distance;
            idHeapArray[currentSize] = id;
            System.arraycopy(heapPosition, insertIdx, heapPosition, insertIdx + 1, currentSize - insertIdx);
            heapPosition[insertIdx] = currentSize;
            heapifyBottomUp(currentSize);
            currentSize++;
        }
    }

    /**
     * restores the heap property of the array after removing the first element.
     * @param n the length of the array
     * @param root the position in the array of the root of the subtree to heapify
     */
    private void heapifyTopDown(int n, int root) {
        int smallest = root; // Initialize smallest as root
        int leftChild = 2 * root + 1;
        int rightChild = 2 * root + 2;

        // If left child is larger than root
        if (leftChild < n && compareValues(leftChild, smallest) < 0) {
            smallest = leftChild;
        }

        // If right child is larger than smallest so far
        if (rightChild < n && compareValues(rightChild, smallest) < 0) {
            smallest = rightChild;
        }

        // If smallest is not root
        if (smallest != root) {
            swap(root, smallest);

            // Recursively heapify the affected sub-tree
            heapifyTopDown(n, smallest);
        }
    }

    /**
     * restores the heap property of the array after adding another element. also used to update the
     * position of an id already contained in the array.
     * @param nodeID the position in the array of the node to check
     */
    private void heapifyBottomUp(int nodeID) {
        if(nodeID <= 0) {
            return;
        }
        // Find parent
        int parent = (nodeID - 1) / 2;

        // For Min-Heap
        // If current node is greater than its parent
        // Swap both of them and call heapify again
        // for the parent
        if (compareValues(nodeID, parent) < 0) {
            swap(nodeID, parent);
            // Recursively heapify the parent node
            heapifyBottomUp(parent);
        }
    }

    /**
     * swaps two elements within the array. Also updates the heap positions of these elements.
     * @param i the position of the first element
     * @param j the position of the second element
     */
    private void swap(int i, int j) {
        int tmp = idHeapArray[i];
        idHeapArray[i] = idHeapArray[j];
        idHeapArray[j] = tmp;

        //also update positions in heap information
        int positionIdxI = Arrays.binarySearch(containedIds, idHeapArray[i]);
        int positionIdxJ = Arrays.binarySearch(containedIds, idHeapArray[j]);
        tmp = heapPosition[positionIdxI];
        heapPosition[positionIdxI] = heapPosition[positionIdxJ];
        heapPosition[positionIdxJ] = tmp;
    }

    /**
     * increases the size of the heap array.
     */
    private void grow() {
        int oldLen = idHeapArray.length;
        idHeapArray = Arrays.copyOf(idHeapArray, oldLen + SIZE_INCREASE);
        distances = Arrays.copyOf(distances, oldLen + SIZE_INCREASE);
        containedIds = Arrays.copyOf(containedIds, oldLen + SIZE_INCREASE);
        Arrays.fill(containedIds, oldLen, containedIds.length, Integer.MAX_VALUE);
        heapPosition = Arrays.copyOf(heapPosition, oldLen + SIZE_INCREASE);
        Arrays.fill(containedIds, oldLen, containedIds.length, Integer.MAX_VALUE); //make sure binary search works
    }

    /**
     * compares the distances from the start node of elements at specific positions on the heap.
     * Returns a number indicating the relation of the distances.
     * @param firstID the first position on the heap
     * @param secondID the first position on the heap
     * @return 0 if equal, 1 if distance of first element is larger, else -1
     */
    private int compareValues(int firstID, int secondID) {
        int positionIdx1 = Arrays.binarySearch(containedIds, idHeapArray[firstID]);
        int positionIdx2 = Arrays.binarySearch(containedIds, idHeapArray[secondID]);
        if(positionIdx1 < 0) { //todo: remove
            System.out.println("ttt: negative idx: " + idHeapArray[firstID]);
        }
        if(positionIdx2 < 0) { //todo: remove
            System.out.println("ttt: negative idx: " + idHeapArray[secondID]);
        }
        if(distances[positionIdx1] == distances[positionIdx2]) {
            return 0;
        } else if(distances[positionIdx1] > distances[positionIdx2]) {
            return 1;
        } else {
            return -1;
        }
    }
}
