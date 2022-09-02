package de.fmi.searouter.utils;

import java.util.Arrays;

public class IntStack {
    private int[] elements;
    private int elementCount;
    private int fifoIdx;
    private final int sizeIncrease;

    public IntStack(int size) {
        this.sizeIncrease = size;
        this.elementCount = 0;
        fifoIdx = 0;
        this.elements = new int[size];
    }

    public void clear() {
        elementCount = 0;
        fifoIdx = 0;
    }

    public int popFifo() {
        int retVal = elements[fifoIdx];
        fifoIdx++;
        return retVal;
    }

    public int pop() {
        elementCount--;
        return elements[elementCount];
    }

    public void push(int toAdd) {
        if(elementCount == elements.length) {
            grow();
        }
        elements[elementCount] = toAdd;
        elementCount++;
    }

    public boolean isEmpty() {
        return elementCount == fifoIdx;
    }

    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
    }
}
