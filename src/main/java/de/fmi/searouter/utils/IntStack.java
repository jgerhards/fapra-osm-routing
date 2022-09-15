package de.fmi.searouter.utils;

import java.util.Arrays;

/**
 * Stack implementation based on primitive int type. Also capable of returning in a fifo order.
 */
public class IntStack {
    //elements currently on the stack
    private int[] elements;
    //next index an element will be inserted at
    private int nextIdx;
    //the index of the element the next fifo pop will return
    private int fifoIdx;
    //the size by which elements will be increased by if necessary
    private final int sizeIncrease;

    /**
     * Constructor. Creates a new object with a given initial size. This is also used as a future size increase.
     * @param size the initial size and size increase
     */
    public IntStack(int size) {
        this.sizeIncrease = size;
        this.nextIdx = 0;
        fifoIdx = 0;
        this.elements = new int[size];
    }

    /**
     * Empty the stack and prepare for reuse.
     */
    public void clear() {
        nextIdx = 0;
        fifoIdx = 0;
    }

    /**
     * Get the next element in FIFO order. Also update the stack so the next time this function is called, the next
     * element is returned.
     * @return the next element in FIFO order
     */
    public int popFifo() {
        int retVal = elements[fifoIdx];
        fifoIdx++;
        return retVal;
    }

    /**
     * Pop an element from the stack. Also update the stack so the next time this function is called, the correct
     * element will be affected.
     * @return the next element
     */
    public int pop() {
        nextIdx--;
        return elements[nextIdx];
    }

    /**
     * Push a new element to the stack.
     * @param toAdd the element to push
     */
    public void push(int toAdd) {
        if(nextIdx == elements.length) {
            grow();
        }
        elements[nextIdx] = toAdd;
        nextIdx++;
    }

    /**
     * Check if there are still elements on the stack (that were not previously popped)
     * @return true if the stack is empty, else false
     */
    public boolean isEmpty() {
        //due to us supporting fifo return order as well, a check for == 0 is insufficient
        return nextIdx == fifoIdx;
    }

    /**
     * Increase the size of memory available to store elements.
     */
    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
    }
}
