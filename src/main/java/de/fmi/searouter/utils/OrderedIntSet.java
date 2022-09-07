package de.fmi.searouter.utils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Set capable of storing primitive int type values. Note that it is available in a sorted and unsorted variant.
 * Some functions should only be called for one of these versions (or in some other special cases). As this class
 * is used during routing, no checks for this are included in order to speed up requests. However, if not
 * followed properly, undefined behaviour may occur in some cases.
 */
public class OrderedIntSet implements Serializable {
    //true if the set is of the ordered variant
    private final boolean orderedBySorting;
    //currently contained elements
    private int[] elements;
    //number of elements currently contained
    private int elementCount;
    //the size by which elements will be increased by if necessary
    private final int sizeIncrease;

    /**
     * Constructor. Creates a new object with a given initial size and future size increase. Also chooses if
     * the set should be ordered or not.
     * @param initialSize the initial size
     * @param sizeIncrease the future size increase
     * @param orderedBySorting true if the set should be ordered, else false
     */
    public OrderedIntSet(boolean orderedBySorting, int initialSize, int sizeIncrease) {
        this.orderedBySorting = orderedBySorting;
        this.sizeIncrease = sizeIncrease;
        this.elements = new int[initialSize];
        elementCount = 0;
        if(orderedBySorting) {
            //in this case, we have to make sure binary search will work
            Arrays.fill(elements, Integer.MAX_VALUE);
        }
    }

    /**
     * Insert a new element at the tail of the set. In most cases, should only be used if the set is not
     * ordered or empty.
     * @param toAdd the element to add
     */
    public void insertTail(int toAdd) {
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
        elements[elementCount] = Integer.MAX_VALUE; //just in case the set is ordered
    }

    /**
     * Insert an element at a given index. After this operation, elements which previously had a higher index
     * will have their index increased by one. In most cases, this function should only be used if the set is
     * not ordered.
     * @param toAdd the element to add
     * @param idx the index at which to add the element
     */
    public void insertAtIdx(int toAdd, int idx) {
        if(elementCount == elements.length) {
            grow();
        }
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
    }

    /**
     * Insert a new element into the set. This function should only be called for an ordered set.
     *  After this operation, elements which previously had a higher index will have their index increased by one.
     * @param toAdd the element to add
     * @return the index of the new element or -1 if it was already contained
     */
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

    /**
     * Get an element stored at a given index.
     * @param idx the index
     * @return the element at this index
     */
    public int get(int idx) {
        if(idx >= elementCount) {
            throw new IndexOutOfBoundsException();
        }
        return elements[idx];
    }

    /**
     * Get the number of elements currently contained in the set.
     * @return the number of elements
     */
    public int size() {
        return elementCount;
    }

    /**
     * Get the index a given element is stored at. If no such element is present,
     * the index at which it would be inserted *(-1) and decreased by one will be returned (similar to
     * Arrays.binarySearch). This operation should only be called if the set is ordered.
     * @param element the element to search
     * @return the index of the element
     */
    public int getIdx(int element) {
        return Arrays.binarySearch(elements, element);
    }

    /**
     * Update a value at a given index. In most cases, this operation should only be used for unordered sets.
     * @param newVal the new value
     * @param idx the index
     */
    public void updateValue(int newVal, int idx) {
        elements[idx] = newVal;
    }

    /**
     * Make sure a given number of elements will fit into the set. This function exists to prevent multiple
     * calls to grow() if the (worst case) number of elements to be inserted is already known.
     * @param numOfElements the number of elements which should fit.
     */
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

    /**
     * Clear the set. After this, it can be reused without the need to allocate memory for a new object.
     */
    public void clear() {
        elementCount = 0;
        if(orderedBySorting) {
            //make sure binary search will work
            Arrays.fill(elements, Integer.MAX_VALUE);
        }
    }

    /**
     * Get an array of all elements in this set.
     * @return an array of all elements in the set
     */
    public int[] toArray() {
        int[] asArray = new int[elementCount];
        System.arraycopy(elements, 0, asArray, 0, elementCount);
        return asArray;
    }

    /**
     * Increase the size of memory available to store elements.
     */
    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
        if(orderedBySorting) {
            //make sure binary search will work
            Arrays.fill(elements, oldLen, elements.length, Integer.MAX_VALUE);
        }
    }

}
