package de.fmi.searouter.utils;

import java.util.Arrays;

public class IntArraySet {
    private int[] elements;
    private int elementCount;
    private final int sizeIncrease;

    public IntArraySet(int size, int sizeIncrease) {
        this.sizeIncrease = sizeIncrease;
        this.elementCount = 0;
        this.elements = new int[size];
        Arrays.fill(elements, Integer.MAX_VALUE);
    }

    public boolean contains(int element) {
        return Arrays.binarySearch(elements, element) >= 0;
    }

    public void add(int toAdd) {
        int idx = Arrays.binarySearch(elements, toAdd);
        if(idx >= 0) {
            return;
        }

        if(elementCount == elements.length) {
            grow();
        }
        idx = (idx + 1) * (-1);
        if(elementCount - idx < 0) {
            System.out.println("a a");
        }
        System.arraycopy(elements, idx, elements, idx + 1, elementCount - idx);
        elements[idx] = toAdd;
        elementCount++;
    }

    public int getLen() {
        return elementCount;
    }

    private void grow() {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + sizeIncrease);
        Arrays.fill(elements, oldLen, elements.length, Integer.MAX_VALUE);
    }
}
