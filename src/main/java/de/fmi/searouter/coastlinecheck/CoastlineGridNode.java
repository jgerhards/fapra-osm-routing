package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.domain.IntersectionHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Inner node of the grid for a coastline check. This type of sub-node contains an array of other nodes which partition
 * the area of this node. Also, it contains a level in order to find to which sub-node a given point belongs to.
 */
public class CoastlineGridNode extends CoastlineGridElement {

    private static final double THRESHOLD = 0.000000001;
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
    CoastlineGridNode(int level, double lowLatBound, double lowLongBound, Set<Integer> coastlineIDs) {
        //System.out.println("constructor of grid node called, list size = " + coastlineIDs.size());
        this.level = level;
        subNodes = new CoastlineGridElement[10][10];  //todo: check these dimensions

        double highLatBound = lowLatBound + Math.pow(10, ((-1) * level) + 1);
        double stepSize = Math.pow(10, (-1) * level);
        double highLongBound = lowLongBound + Math.pow(10, ((-1) * level) + 1);
        //account for rounding errors with double values
        highLatBound = highLatBound + THRESHOLD;
        highLongBound = highLongBound - THRESHOLD;

        //create sub-grid (may be stored on hard drive and imported from there later on)
        //first, get coastline intersections with lines along the latitude (constant latitude)
        List<Integer>[][] coastlineIntersectionsLatitude = new List[11][10];  //todo: check these dimensions
        int listLatIdx = 0;
        for (double iteratingLatitude = lowLatBound; iteratingLatitude <= highLatBound; iteratingLatitude += stepSize) {
            int listLongIdx = 0;
            for (double iteratingLongitude = lowLongBound; iteratingLongitude < highLongBound;
                 iteratingLongitude += stepSize) {
                //System.out.println("ttt: node loop values lat: "+iteratingLongitude+" "+highLongBound+" "+stepSize);
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
                        //System.out.println("ttt: lines intersect");
                        coastlineIntersectionsLatitude[listLatIdx][listLongIdx].add(i);
                    }
                }
                listLongIdx++;
            }
            listLatIdx++;
        }

        //now we need to account for rounding errors with double values the other way around
        highLatBound = highLatBound - 2*THRESHOLD;
        highLongBound = highLongBound + 2*THRESHOLD;

        //now, calculate the same thing for intersection with lines along the longitude
        List<Integer>[][] coastlineIntersectionsLongitude = new List[10][11];
        listLatIdx = 0;
        for (double iteratingLatitude = lowLatBound; iteratingLatitude < highLatBound; iteratingLatitude += stepSize) {
            int listLongIdx = 0;
            for (double iteratingLongitude = lowLongBound; iteratingLongitude <= highLongBound;
                 iteratingLongitude += stepSize) {
                //System.out.println("ttt: node loop values long: "+iteratingLongitude+" "+highLongBound+" "+stepSize);
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

        //check if lengths of lists is different
        for(int i = 0; i < coastlineIntersectionsLatitude.length; i++) {
            for(int j = 0; j < coastlineIntersectionsLatitude[i].length; j++) {
                //System.out.println("ttt: latListLengths " + coastlineIntersectionsLatitude[i][j].size()+" "+i+" "+j);
            }
        }
        for(int i = 0; i < coastlineIntersectionsLongitude.length; i++) {
            for(int j = 0; j < coastlineIntersectionsLongitude[i].length; j++) {
                //System.out.println("ttt: longListLengths " + coastlineIntersectionsLongitude[i][j].size()+" "+i+" "+j);
            }
        }

        //now back to rounding down for both values
        highLongBound = highLongBound - 2*THRESHOLD;
        //fill array with new grid elements
        int arrayLatIdx = 0;
        for (double lowLatBoundLoop = lowLatBound; lowLatBoundLoop < highLatBound; lowLatBoundLoop += stepSize) {
            double highLatBoundLoop = lowLatBoundLoop + stepSize;
            int arrayLongIdx = 0;
            for (double lowLongBoundLoop = lowLongBound; lowLongBoundLoop < highLongBound;
                 lowLongBoundLoop += stepSize) {
                double highLongBoundLoop = lowLongBoundLoop + stepSize;  //todo: can this variable be removed?
                //System.out.println("ttt: low bound"+lowLatBoundLoop+" high bound: "+highLatBoundLoop);
                //System.out.println("ttt: latIdx: "+arrayLatIdx+" longIdx: "+arrayLongIdx);
                Set<Integer> coastlinesInArea = new LinkedHashSet<>();
                //System.out.println("ttt: set reset, size = " + coastlinesInArea.size());
                //add coastlines by intersection with longitudinal lines
                coastlinesInArea.addAll(coastlineIntersectionsLongitude[arrayLatIdx][arrayLongIdx]);
                coastlinesInArea.addAll(coastlineIntersectionsLongitude[arrayLatIdx][arrayLongIdx + 1]);

                //add coastlines by intersection with latitudinal lines
                if(Math.abs(lowLatBoundLoop-(-90.0)) < 2*THRESHOLD) {  //todo: larger threshold to account for threshold calculation before
                    //only north of area relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx][arrayLongIdx]);
                } else if(Math.abs(highLatBoundLoop - 90.0) < 2*THRESHOLD){
                    //only south of area relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx - 1][arrayLongIdx]);
                } else {
                    //both north and south of area is relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx][arrayLongIdx]);
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx + 1][arrayLongIdx]);
                }

                for (int i : coastlineIDs) {
                    //check for start points of coastlines in the area (for lines without intersection with border)
                    //only one point has to be checked, if only one is in area intersection will find the coastline
                    double coastlineStartLat = Coastlines.getStartLatitude(i);
                    double coastlineStartLong = Coastlines.getStartLongitude(i);
                    if(coastlineStartLat >= lowLatBoundLoop && coastlineStartLat <= highLatBoundLoop &&
                            coastlineStartLong >= lowLongBoundLoop && coastlineStartLong <= highLongBoundLoop) {
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
        int latIdx;
        int longIdx;

        //first, check for special cases which would lead to problems. Else use default method
        if(latitude == -90) {
            latIdx = 0;
        } else if(latitude == 90) {
            latIdx = 9;
        } else {
            latIdx = getDefaultIdx(latitude);
        }

        if(longitude == -180) {
            longIdx = 0;
        } else if(longitude == 180) {
            longIdx = 9;
        } else {
            longIdx = getDefaultIdx(longitude);
        }
        return subNodes[latIdx][longIdx].pointIsInWater(latitude, longitude);
    }

    private int getDefaultIdx(double coordinate) {
        boolean coordinateNegative = false;
        int idx;

        //work only with positive numbers to make sure modulo operator works as intended
        if(coordinate < 0) {
            coordinate = coordinate * (-1);
            coordinateNegative = true;
        }

        //remove more significant digits
        double relevantPart = coordinate % Math.pow(10, ((-1) * level) + 1);

        //remove less significant digits
        relevantPart = relevantPart * Math.pow(10, level);
        idx = (int) Math.floor(relevantPart);

        //correct for negative coordinates
        if(coordinateNegative) {
            idx = 9 - idx;
        }
        return idx;
    }
}
