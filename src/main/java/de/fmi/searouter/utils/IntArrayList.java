package de.fmi.searouter.utils;

import java.util.Arrays;

public class IntArrayList {
    private int[] elements;
    private int elementCount;
    private final int sizeIncrease;

    public IntArrayList(int size) {
        this.sizeIncrease = size;
        this.elementCount = 0;
        this.elements = new int[size];
    }

    public int get(int idx) {
        if(idx >= elementCount) {
            throw new IndexOutOfBoundsException();
        }
        return elements[idx];
    }

    public void add(int toAdd) {
        if(elementCount == elements.length) {
            grow(1);
        }
        elements[elementCount] = toAdd;
        elementCount++;
    }

    public int getLen() {
        return elementCount;
    }

    public void clear() {
        elementCount = 0;
    }

    public void addAll(IntArrayList toAdd) {
        int len = toAdd.getLen();
        int sizeFactor = (len / this.sizeIncrease) + 1;
        grow(sizeFactor);

        for (int i = 0; i < len; i++) {
            elements[elementCount] = toAdd.get(i);
            elementCount++;
        }
    }

    private void grow(int times) {
        int oldLen = elements.length;
        elements = Arrays.copyOf(elements, oldLen + (sizeIncrease * times));
    }
}
