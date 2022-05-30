package de.fmi.searouter.router;

import de.fmi.searouter.grid.Node;

import java.util.Arrays;

public class DijkstraHeap {
    private final int INITIAL_SIZE = 400;
    private final int SIZE_INCREASE = 200;
    private final int[] heapPosition;
    private final DijkstraRouter router;
    private int[] idHeapArray;
    private int currentSize;

    protected DijkstraHeap(DijkstraRouter router) {
        this.heapPosition = new int[Node.getSize()];
        this.idHeapArray = new int[INITIAL_SIZE];
        Arrays.fill(idHeapArray, -1);
        this.router = router;
        currentSize = 0;
    }

    protected void resetState() {
        Arrays.fill(heapPosition, -1);
        //todo: maybe leave it at the current size, in this case remember to reset all values
        this.idHeapArray = new int[INITIAL_SIZE];
        Arrays.fill(idHeapArray, -1);
        currentSize = 0;
    }

    protected boolean isEmpty() {
        return (currentSize == 0);
    }

    protected int getNext() {
        int returnValue = idHeapArray[0];

        currentSize--;
        idHeapArray[0] = idHeapArray[currentSize];
        heapPosition[idHeapArray[0]] = 0;
        idHeapArray[currentSize] = -1;
        heapifyTopDown(currentSize, 0);
        return returnValue;
    }

    protected void add(int id) {
        if(heapPosition[id] != -1) {
            //update, do not add again
            heapifyBottomUp(heapPosition[id]);
        } else {
            if(currentSize == idHeapArray.length) {
                grow();
            }
            idHeapArray[currentSize] = id;
            heapPosition[id] = currentSize;
            heapifyBottomUp(currentSize);
            currentSize++;
        }
    }

    /*protected void printSize() {
        System.out.println("ttt: heapsize: " + idHeapArray.length);
    }

    protected void print() {
        String str = "";
        for(int i = 0; i < currentSize; i++) {
            str += idHeapArray[i] + ", ";
        }
        System.out.println("ttt: " + str);
    }*/

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

    private void swap(int i, int j) {
        int tmp = idHeapArray[i];
        idHeapArray[i] = idHeapArray[j];
        idHeapArray[j] = tmp;

        //also update positions in heap information
        tmp = heapPosition[idHeapArray[i]];
        heapPosition[idHeapArray[i]] = heapPosition[idHeapArray[j]];
        heapPosition[idHeapArray[j]] = tmp;
    }

    private void grow() {
        int oldLen = idHeapArray.length;
        idHeapArray = Arrays.copyOf(idHeapArray, oldLen + SIZE_INCREASE);
        Arrays.fill(idHeapArray, oldLen, idHeapArray.length, -1);
    }

    private int compareValues(int firstID, int secondID) {
        if(router.currDistanceToNode[idHeapArray[firstID]] == router.currDistanceToNode[idHeapArray[secondID]]) {
            return 0;
        } else if(router.currDistanceToNode[idHeapArray[firstID]] > router.currDistanceToNode[idHeapArray[secondID]]) {
            return 1;
        } else {
            return -1;
        }
    }
}
