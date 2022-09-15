package de.fmi.searouter.utils;

import java.util.Arrays;

/**
 * Set keeping track of which elements are inserted so far. Based on primitive int type.
 */
public class IntArraySet {
    //(sorted) elements in this set
    private int[] elements;
    //the number of elements currently contained
    private int elementCount;
    //the size to increase elements by if necessary
    private final int sizeIncrease;

    /**
     * Constructor. Create a new object with a given initial size and size increase.
     * @param size the initial size
     * @param sizeIncrease the size increase
     */
    public IntArraySet(int size, int sizeIncrease) {
        this.sizeIncrease = sizeIncrease;
        this.elementCount = 0;
        this.elements = new int[size];
        Arrays.fill(elements, Integer.MAX_VALUE);
    }

    /**
     * Check if an element is contained in the set.
     * @param element the element to check for
     * @return true if the element is contained, else false
     */
    public boolean contains(int element) {
        return Arrays.binarySearch(elements, element) >= 0;
    }

    /**
     * Insert a new element into the set.
     * @param toAdd the element to add
     */
    public void add(int toAdd) {
        int idx = Arrays.binarySearch(elements, toAdd);
        if(idx >= 0) {
            //element already contained, no need to add again
            return;
        }

        if(elementCount == elements.length) {
            grow();
        }
        idx = (idx + 1) * (-1);
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
    }

    /**
     * Increase the size of memory available to store elements.
     */
    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
        Arrays.fill(elements, oldLen, elements.length, Integer.MAX_VALUE);
    }
}
