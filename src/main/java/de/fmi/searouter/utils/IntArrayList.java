package de.fmi.searouter.utils;

import java.util.Arrays;

/**
 * Ordered list of primitive ints based on arrays.
 */
public class IntArrayList {
    //elements contained
    private int[] elements;
    //number of elements currently in the structure
    private int elementCount;
    //size elements will be increased by if more space is needed
    private final int sizeIncrease;

    /**
     * Constructor. Create a new object and configures the size and size increase.
     * @param size the initial size. will also be used as size increase
     */
    public IntArrayList(int size) {
        this.sizeIncrease = size;
        this.elementCount = 0;
        this.elements = new int[size];
    }

    /**
     * Get the element at a given index.
     * @param idx the index
     * @return the element at the given index
     */
    public int get(int idx) {
        if(idx >= elementCount) {
            throw new IndexOutOfBoundsException();
        }
        return elements[idx];
    }

    /**
     * Insert a new element.
     * @param toAdd the element to insert
     */
    public void add(int toAdd) {
        if(elementCount == elements.length) {
            grow();
        }
        elements[elementCount] = toAdd;
        elementCount++;
    }

    /**
     * Get the number of elements currently in the data structure.
     * @return the current number of elements
     */
    public int getLen() {
        return elementCount;
    }

    /**
     * clear the data structure.
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
