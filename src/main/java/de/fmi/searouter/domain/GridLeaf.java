package de.fmi.searouter.domain;

import java.util.List;

public class GridLeaf extends GridCell {

    private int[] edgeIds;

    private boolean isCenterPointInWater;

    private double latCenterPoint;
    private double lonCenterPoint;

    public GridLeaf(int[] edgeIds) {
        this.edgeIds = edgeIds;
    }

    @Override
    public void initializeCellBorders() {

    }

    @Override
    public void initCenterPoint() {

    }

    @Override
    public boolean isPointInWater(float lat, float lon) {
        return false;
    }

    @Override
    public List<Integer> getContainedEdgesIDs() {
        return null;
    }
}
