package de.fmi.searouter.utils;

import java.io.Serializable;
import java.util.Arrays;

public class OrderedIntSet implements Serializable {
    private int[] elements;
    private int elementCount;
    private final boolean orderedBySorting;
    private final int sizeIncrease;

    public OrderedIntSet(boolean orderedBySorting, int initialSize, int sizeIncrease) {
        this.orderedBySorting = orderedBySorting;
        this.sizeIncrease = sizeIncrease;
        this.elements = new int[initialSize];
        elementCount = 0;
        if(orderedBySorting) {
            Arrays.fill(elements, Integer.MAX_VALUE);
        }
    }

    public void insertTail(int toAdd) {
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

    public void insertAtIdx(int toAdd, int idx) {
        if(elementCount == elements.length) {
            grow();
        }
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
    }

    public int insertSorted(int toAdd) {
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

    public int get(int idx) {
        if(idx >= elementCount) {
            throw new IndexOutOfBoundsException();
        }
        return elements[idx];
    }

    public int size() {
        return elementCount;
    }

    public boolean contains(int element) {
        return (Arrays.binarySearch(elements, element) >= 0);
    }

    public int getIdx(int element) {
        return Arrays.binarySearch(elements, element);
    }

    public void updateValue(int newVal, int idx) {
        elements[idx] = newVal;
    }

    public void makeSpace(int numOfElements) {
        int oldLen = elements.length;
        int freeElements = oldLen - elementCount;
        if(freeElements < numOfElements) {
            elements = Arrays.copyOf(elements, elements.length + numOfElements);
            if(orderedBySorting) {
                Arrays.fill(elements, oldLen, elements.length, Integer.MAX_VALUE);
            }
        }
    }

    public void clear() {
        elementCount = 0;
        if(orderedBySorting) {
            Arrays.fill(elements, Integer.MAX_VALUE);
        }
    }

    public int[] toArray() {
        int[] asArray = new int[elementCount];
        System.arraycopy(elements, 0, asArray, 0, elementCount);
        return asArray;
    }

    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
        if(orderedBySorting) {
            Arrays.fill(elements, oldLen, elements.length, Integer.MAX_VALUE);
        }
    }

}
