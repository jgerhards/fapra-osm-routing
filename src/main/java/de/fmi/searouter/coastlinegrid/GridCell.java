package de.fmi.searouter.coastlinegrid;

import de.fmi.searouter.dijkstragrid.GridNode;
import de.fmi.searouter.importdata.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class GridCell {

    public enum ApproachDirection {
        FROM_HORIZONTAL, FROM_VERTICAL
    }

    public abstract void setCenterPoint(double lat, double lon, boolean isInWater);

    protected static int EDGE_THRESHOLD = 1000;

    public abstract boolean initCenterPoint(double originCenterPointLat, double originCenterPointLon,
                                         boolean originCenterPointInWater, Set<Integer> additionalEdges,
                                         ApproachDirection dir);

    public abstract boolean isPointInWater(float lat, float lon);

    public abstract Set<Integer> getAllContainedEdgeIDs();

    public abstract GridNode getCenterPoint();

    public abstract void getAllCenterPoints(int currDepth, int maxDepth, List<GridNode> pointList);

    public abstract double getCtrLat();

    public abstract double getCtrLon();


}
