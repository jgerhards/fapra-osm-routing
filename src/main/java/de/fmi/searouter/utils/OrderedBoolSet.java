package de.fmi.searouter.utils;

import java.io.Serializable;
import java.util.Arrays;

public class OrderedBoolSet implements Serializable {
    private boolean[] elements;
    private int elementCount;
    private final int sizeIncrease;

    public OrderedBoolSet(int initialSize, int sizeIncrease) {
        this.sizeIncrease = sizeIncrease;
        this.elements = new boolean[initialSize];
        elementCount = 0;
    }

    public void insertTail(boolean toAdd) {
        if(elementCount == elements.length) {
            grow();
        }
        elements[elementCount] = toAdd;
        elementCount++;
    }

    public void removeAtIdx(int idx) {
        elementCount--;
        System.arraycopy(elements, idx + 1, elements, idx, elementCount - idx);
    }

    public void insertAtIdx(boolean toAdd, int idx) {
        if(elementCount == elements.length) {
            grow();
        }
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
    }

    public void updateValue(boolean newVal, int idx) {
        elements[idx] = newVal;
    }

    public boolean get(int idx) {
        if(idx >= elementCount) {
            throw new IndexOutOfBoundsException();
        }
        return elements[idx];
    }

    public int size() {
        return elementCount;
    }

    public void clear() {
        elementCount = 0;
    }

    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
    }

}
