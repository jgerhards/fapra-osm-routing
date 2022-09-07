package de.fmi.searouter.utils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Set capable of storing primitive boolean type values.
 */
public class OrderedBoolSet implements Serializable {
    //currently contained elements
    private boolean[] elements;
    //number of elements currently contained
    private int elementCount;
    //the size by which elements will be increased by if necessary
    private final int sizeIncrease;

    /**
     * Constructor. Creates a new object with a given initial size and future size increase.
     * @param initialSize the initial size
     * @param sizeIncrease the future size increase
     */
    public OrderedBoolSet(int initialSize, int sizeIncrease) {
        this.sizeIncrease = sizeIncrease;
        this.elements = new boolean[initialSize];
        elementCount = 0;
    }

    /**
     * Insert a new element at the tail of the set.
     * @param toAdd the element to add
     */
    public void insertTail(boolean toAdd) {
        if(elementCount == elements.length) {
            grow();
        }
        elements[elementCount] = toAdd;
        elementCount++;
    }

    /**
     * Remove an element at a given index. After this operation, elements which previously had a higher index
     * will have their index reduced by one.
     * @param idx the index of the element to remove
     */
    public void removeAtIdx(int idx) {
        elementCount--;
        System.arraycopy(elements, idx + 1, elements, idx, elementCount - idx);
    }

    /**
     * Insert an element at a given index. After this operation, elements which previously had a higher index
     * will have their index increased by one.
     * @param toAdd the element to add
     * @param idx the index at which to add the element
     */
    public void insertAtIdx(boolean toAdd, int idx) {
        if(elementCount == elements.length) {
            grow();
        }
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
    }

    /**
     * Update a value at a given index.
     * @param newVal the new value
     * @param idx the index
     */
    public void updateValue(boolean newVal, int idx) {
        elements[idx] = newVal;
    }

    /**
     * Get an element stored at a given index.
     * @param idx the index
     * @return the element at this index
     */
    public boolean get(int idx) {
        if(idx >= elementCount) {
            throw new IndexOutOfBoundsException();
        }
        return elements[idx];
    }

    /**
     * Clear the set. After this, it can be reused without the need to allocate memory for a new object.
     */
    public void clear() {
        elementCount = 0;
    }

    /**
     * Increase the size of memory available to store elements.
     */
    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
    }

}
