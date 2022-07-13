package de.fmi.searouter.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class GridCell {

    protected static int EDGE_THRESHOLD = 50;

    public abstract void initializeCellBorders();

    public abstract void initCenterPoint();

    public abstract boolean isPointInWater(float lat, float lon);

    public abstract List<Integer> getContainedEdgesIDs();


}
