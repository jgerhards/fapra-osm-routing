package de.fmi.searouter.utils;

import java.io.Serializable;
import java.util.Arrays;

public class OrderedDoubleSet implements Serializable {
    private double[] elements;
    private int elementCount;
    private final boolean orderedBySorting;
    private final int sizeIncrease;

    public OrderedDoubleSet(boolean orderedBySorting, int initialSize, int sizeIncrease) {
        this.orderedBySorting = orderedBySorting;
        this.sizeIncrease = sizeIncrease;
        this.elements = new double[initialSize];
        elementCount = 0;
        if(orderedBySorting) {
            Arrays.fill(elements, Double.MAX_VALUE);
        }
    }

    public void insertTail(double toAdd) {
        if(orderedBySorting) {
            throw new IllegalStateException();
        }
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

    public void insertAtIdx(double toAdd, int idx) {
        if(orderedBySorting) {
            throw new IllegalStateException();
        }
        if(elementCount == elements.length) {
            grow();
        }
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
    }

    public int insertSorted(double toAdd) {
        if(!orderedBySorting) {
            throw new IllegalStateException();
        }
        if(elementCount == elements.length) {
            grow();
        }
        int idx = Arrays.binarySearch(elements, toAdd);
        if(idx >= 0) {
            //this should not happen
            return -1;
        }
        idx = (idx + 1) * (-1);
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
        return idx;
    }

    public double get(int idx) {
        if(idx >= elementCount) {
            throw new IndexOutOfBoundsException();
        }
        return elements[idx];
    }

    public int size() {
        return elementCount;
    }

    public boolean contains(double element) {
        return (Arrays.binarySearch(elements, element) >= 0);
    }

    public int getIdx(int element) {
        return Arrays.binarySearch(elements, element);
    }

    public void updateValue(double newVal, int idx) {
        elements[idx] = newVal;
    }

    public void clear() {
        elementCount = 0;
    }

    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
        if(orderedBySorting) {
            Arrays.fill(elements, oldLen, elements.length, Double.MAX_VALUE);
        }
    }

}
