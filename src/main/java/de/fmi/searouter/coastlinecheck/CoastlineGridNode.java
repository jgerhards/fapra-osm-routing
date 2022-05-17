package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.domain.IntersectionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Inner node of the grid for a coastline check. This type of sub-node contains an array of other nodes which partition
 * the area of this node. Also, it contains a level in order to find to which sub-node a given point belongs to.
 */
public class CoastlineGridNode extends CoastlineGridElement {

    //nodes in the next lower level
    private CoastlineGridElement[][] subNodes;
    //level used to determine which decimal places of the point coordinates are relevant for the next partition
    private int level;

    /**
     * creates a new CoastlineGridNode. This also recursively calls itself to generate the entire hierarchy.
     * @param level the level of this node
     * @param lowLatBound the low bound for the longitude of this area.
     * @param lowLongBound the low bound for the latitude of this area.
     * @param coastlineIDs a list of all coastlines within this area
     */
    CoastlineGridNode(int level, double lowLatBound, double lowLongBound, List<Integer> coastlineIDs) {
        this.level = level;
        subNodes = new CoastlineGridElement[10][10];  //todo: check these dimensions

        double highLatBound = lowLatBound + Math.pow(10, ((-1) * level) + 1);
        double stepSize = Math.pow(10, (-1) * level);
        double highLongBound = lowLongBound + Math.pow(10, ((-1) * level) + 1);

        //create sub-grid (may be stored on hard drive and imported from there later on)
        //first, get coastline intersections with lines along the latitude (constant latitude)
        List<Integer>[][] coastlineIntersectionsLatitude = new List[11][10];  //todo: check these dimensions
        int listLatIdx = 0;
        for (double iteratingLatitude = lowLatBound; iteratingLatitude <= highLatBound; iteratingLatitude += stepSize) {
            int listLongIdx = 0;
            for (double iteratingLongitude = lowLongBound; iteratingLongitude < highLongBound;
                 iteratingLongitude += stepSize) {
                //todo: check if indices of coastlineIntersectionsLatitude are given correctly
                coastlineIntersectionsLatitude[listLatIdx][listLongIdx] = new ArrayList<Integer>();
                for (int i : coastlineIDs) {
                    double lineStartLongitude = iteratingLongitude;
                    double lineEndLongitude = iteratingLongitude + stepSize;
                    double lineLatitude = iteratingLatitude;

                    double coastLineStartLatitude = Coastlines.getStartLatitude(i);
                    double coastLineStartLongitude = Coastlines.getStartLongitude(i);
                    double coastLineEndLatitude = Coastlines.getEndLatitude(i);
                    double coastLineEndLongitude = Coastlines.getEndLongitude(i);

                    boolean coastlineIntersects = IntersectionHelper.linesIntersect(coastLineStartLatitude,
                            coastLineStartLongitude, coastLineEndLatitude, coastLineEndLongitude, lineLatitude,
                            lineStartLongitude, lineLatitude, lineEndLongitude);
                    if (coastlineIntersects) {
                        coastlineIntersectionsLatitude[listLatIdx][listLongIdx].add(i);
                    }
                }
                listLongIdx++;
            }
            listLatIdx++;
        }

        //now, calculate the same thing for intersection with lines along the longitude
        List<Integer>[][] coastlineIntersectionsLongitude = new List[10][11];
        listLatIdx = 0;
        for (double iteratingLatitude = lowLatBound; iteratingLatitude < highLatBound; iteratingLatitude += stepSize) {
            int listLongIdx = 0;
            for (double iteratingLongitude = lowLongBound; iteratingLongitude <= highLongBound;
                 iteratingLongitude += stepSize) {
                //todo: check if indices of coastlineIntersectionsLatitude are given correctly
                coastlineIntersectionsLongitude[listLatIdx][listLongIdx] = new ArrayList<Integer>();
                for (int i : coastlineIDs) {
                    double lineStartLatitude = iteratingLatitude;
                    double lineEndLatitude = iteratingLatitude + stepSize;
                    //no overflow possible for latitude (does not wrap around)
                    double lineLongitude = iteratingLongitude;

                    double coastLineStartLatitude = Coastlines.getStartLatitude(i);
                    double coastLineStartLongitude = Coastlines.getStartLongitude(i);
                    double coastLineEndLatitude = Coastlines.getEndLatitude(i);
                    double coastLineEndLongitude = Coastlines.getEndLongitude(i);

                    boolean coastlineIntersects = IntersectionHelper.linesIntersect(coastLineStartLatitude,
                            coastLineStartLongitude, coastLineEndLatitude, coastLineEndLongitude, lineStartLatitude,
                            lineLongitude, lineEndLatitude, lineLongitude);
                    if (coastlineIntersects) {
                        coastlineIntersectionsLongitude[listLatIdx][listLongIdx].add(i);
                    }
                }
                listLongIdx++;
            }
            listLatIdx++;
        }

        //fill array with new grid elements
        int arrayLatIdx = 0;
        for (double lowLatBoundLoop = lowLatBound; lowLatBoundLoop < highLatBound; lowLatBoundLoop += stepSize) {
            double highLatBoundLoop = lowLatBoundLoop + stepSize;
            int arrayLongIdx = 0;
            for (double lowLongBoundLoop = lowLongBound; lowLongBoundLoop < highLongBound;
                 lowLongBoundLoop += stepSize) {
                double highLongBoundLoop = lowLongBoundLoop + stepSize;
                List<Integer> coastlinesInArea = new ArrayList<>();
                //add coastlines by intersection with longitudinal lines
                coastlinesInArea.addAll(coastlineIntersectionsLongitude[arrayLatIdx][arrayLongIdx]);
                coastlinesInArea.addAll(coastlineIntersectionsLongitude[arrayLatIdx][arrayLongIdx + 1]);

                //add coastlines by intersection with latitudinal lines
                if(lowLatBound == -90.0) {
                    //only north of area relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx][arrayLongIdx]);
                } else if(highLatBound == 90.0){
                    //only south of area relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx - 1][arrayLongIdx]);
                } else {
                    //both north and south of area is relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx - 1][arrayLongIdx]);
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx][arrayLongIdx]);
                }

                for (int i : coastlineIDs) {
                    //check for start points of coastlines in the area (for lines without intersection with border)
                    //only one point has to be checked, if only one is in area intersection will find the coastline
                    double coastlineStartLat = Coastlines.getStartLatitude(i);
                    double coastlineStartLong = Coastlines.getStartLongitude(i);
                    if(coastlineStartLat >= lowLatBound && coastlineStartLat <= highLatBound &&
                            coastlineStartLong >= lowLongBound && coastlineStartLong <= highLongBound) {
                        coastlinesInArea.add(i);
                    }
                }

                //check if we need another grid node or a leaf node
                if(coastlinesInArea.size() <= CoastlineGridElement.MAX_COASTLINES_IN_GRID) {
                    //find reference point (choose middle point)
                    double refLat = (highLatBound + lowLatBound) / 2;
                    double refLong = (highLongBound + lowLongBound) / 2;
                    subNodes[arrayLatIdx][arrayLongIdx] = new CoastlineGridLeaf(refLat, refLong, coastlinesInArea);
                    //todo: at some point, we have to initialize the low level grid. not sure if here or later on better
                } else {
                    subNodes[arrayLatIdx][arrayLongIdx] = new CoastlineGridNode(level + 1, lowLatBound,
                            lowLongBound, coastlinesInArea);
                }
                arrayLongIdx++;
            }
            arrayLatIdx++;
        }

    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public boolean pointIsInWater(double latitude, double longitude) {
        //todo: implement correct version --> call this function on correct sub-node
        return false;
    }
}
