package de.fmi.searouter.utils;

import java.util.Arrays;

/**
 * Heap based on int arrays (primitive type, not the wrapper object). Priority is based on distance, the elements
 * returned are the corresponding ids.
 */
public class DistanceHeap {
    private int INITIAL_SIZE = 10;
    private int SIZE_INCREASE = 5;

    //ordered array of ids currently contained in the heap
    private int[] containedIds;
    //distances associated with the ids. The distance corresponding to an id can be found at the same
    // index as in containedIds
    private int[] distances;
    //for each node, the position of it in the heap, based on the index
    private int[] heapPosition;

    //the array representing the heap
    private int[] idHeapArray;
    //the current number of elements on the heap
    private int currentSize;

    /**
     * Constructor. Initializes the data structures used by this object.
     */
    public DistanceHeap() {
        this.heapPosition = new int[INITIAL_SIZE];
        this.idHeapArray = new int[INITIAL_SIZE];
        this.containedIds = new int[INITIAL_SIZE];
        this.distances = new int[INITIAL_SIZE];
        currentSize = 0;
        //make sure binary search will work
        Arrays.fill(containedIds, Integer.MAX_VALUE);
    }

    /**
     * Constructor. Initializes the data structures used by this object using configured size parameters.
     * @param initialSize the initial size of the data structures
     * @param sizeIncrease the size to increase the data structures by if necessary
     */
    public DistanceHeap(int initialSize, int sizeIncrease) {
        INITIAL_SIZE = initialSize;
        SIZE_INCREASE = sizeIncrease;
        this.heapPosition = new int[INITIAL_SIZE];
        this.idHeapArray = new int[INITIAL_SIZE];
        this.containedIds = new int[INITIAL_SIZE];
        this.distances = new int[INITIAL_SIZE];
        currentSize = 0;
        //make sure binary search will work
        Arrays.fill(containedIds, Integer.MAX_VALUE);
    }

    /**
     * Reset the heap to be empty. After calling this, the heap can be reused without the need to
     * allocate memory again.
     */
    public void reset() {
        currentSize = 0;
        //make sure binary search will work
        Arrays.fill(containedIds, Integer.MAX_VALUE);
    }

    /**
     * Check if the heap is empty.
     * @return true if no more elements are contained on the heap, else false
     */
    public boolean isEmpty() {
        return (currentSize == 0);
    }

    /**
     * Get the id of the node with the lowest distance from the start node stored on the heap. Also restores
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
     * Add an id to the heap. if the id is already on the heap, updates its position if necessary
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
     * Restore the heap property of the array after removing the first element.
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
     * Restore the heap property of the array after adding another element. also used to update the
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
            // Recursively call heapify the parent node
            heapifyBottomUp(parent);
        }
    }

    /**
     * Swap two elements within the array. Also updates the heap positions of these elements.
     * @param i the position of the first element
     * @param j the position of the second element
     */
    private void swap(int i, int j) {
        int tmp = idHeapArray[i];
        idHeapArray[i] = idHeapArray[j];
        idHeapArray[j] = tmp;

        //update positions in heap information
        int positionIdxI = Arrays.binarySearch(containedIds, idHeapArray[i]);
        int positionIdxJ = Arrays.binarySearch(containedIds, idHeapArray[j]);
        tmp = heapPosition[positionIdxI];
        heapPosition[positionIdxI] = heapPosition[positionIdxJ];
        heapPosition[positionIdxJ] = tmp;
    }

    /**
     * Increases the size of the heap array.
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
     * Compare the distances from the start node of elements at specific positions on the heap.
     * Return a number indicating the relation of the distances.
     * @param firstID the first position on the heap
     * @param secondID the first position on the heap
     * @return 0 if equal, 1 if distance of first element is larger, else -1
     */
    private int compareValues(int firstID, int secondID) {
        int positionIdx1 = Arrays.binarySearch(containedIds, idHeapArray[firstID]);
        int positionIdx2 = Arrays.binarySearch(containedIds, idHeapArray[secondID]);
        if(distances[positionIdx1] == distances[positionIdx2]) {
            return 0;
        } else if(distances[positionIdx1] > distances[positionIdx2]) {
            return 1;
        } else {
            return -1;
        }
    }
}
