package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.domain.IntersectionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * class used to check if a given point is in water or on land. Contains the top level of a grid used in
 * this check. This grid is generated within the constructor of this class.
 */
public class CoastlineChecker {

    //grid containing the top level of CoastlineGridElements. index depends on coordinates rounded up to the next 10
    private CoastlineGridElement[][] topLevelGrid;

    /**
     * check if a given point is located on land or in water.
     * @param latitude of the point
     * @param longitude of the point
     * @return true if the point is in water, else false
     */
    public boolean pointIsInWater(double latitude, double longitude) {
        //extract relevant (for this level) parts of coordinates
        int relevantLatitude = (int) Math.floor(latitude);
        int relevantLongitude = (int) Math.floor(longitude);

        int latIdx;
        int longIdx;

        //determine latitude index
        if(relevantLatitude >= 0) {
            relevantLatitude = (relevantLatitude - (relevantLatitude % 10)) / 10;
            latIdx = relevantLatitude + 9;  //there are 9 indices for negative latitude areas

            //latitude 90 is a special case, handle this here
            if(latIdx == 18) {
                latIdx = 17;
            }
        } else {
            relevantLatitude = relevantLatitude * (-1);
            relevantLatitude = (relevantLatitude - (relevantLatitude % 10)) / 10;
            latIdx = 8 - relevantLatitude; //invert due to negative sign

            //latitude -90 is a special case, handle this here
            if(latIdx == -1) {
                latIdx = 0;
            }
        }
        relevantLongitude = (relevantLongitude - (relevantLongitude % 10)) / 10;

        //determine longitude index
        if(relevantLongitude >= 0) {
            relevantLongitude = (relevantLongitude - (relevantLongitude % 10)) / 10;
            longIdx = relevantLongitude + 18;  //there are 18 indices for negative latitude areas

            //longitude 180 is a special case, handle this here
            if(longIdx == 36) {
                longIdx = 35;
            }
        } else {
            relevantLongitude = relevantLongitude * (-1);
            relevantLongitude = (relevantLongitude - (relevantLongitude % 10)) / 10;
            longIdx = 17 - relevantLongitude; //invert due to negative sign

            //longitude -180 is a special case, handle this here
            if(longIdx == -1) {
                longIdx = 0;
            }
        }

        return topLevelGrid[latIdx][longIdx].pointIsInWater(latitude, longitude);
    }

    public CoastlineChecker() {
        topLevelGrid = new CoastlineGridElement[18][36];
        int numberOfCoastlines = Coastlines.getNumberOfWays();

        //create grid (may be stored on hard drive and imported from there later on)
        //first, get coastline intersections with lines along the latitude (constant latitude)
        //latitude has one dimension less as there are only 17 separating lines
        List<Integer>[][] coastlineIntersectionsLatitude = new List[17][36];
        int listLatIdx = 0;

        for (double iteratingLatitude = -80.0; iteratingLatitude <= 80; iteratingLatitude += 10) {
            int listLongIdx = 0;
            for (double iteratingLongitude = -180.0; iteratingLongitude <= 170; iteratingLongitude += 10) {
                System.out.println("ttt: loop 1");
                //todo: check if indices of coastlineIntersectionsLatitude are given correctly
                coastlineIntersectionsLatitude[listLatIdx][listLongIdx] = new ArrayList<Integer>();
                for (int i = 0; i < numberOfCoastlines; i++) {
                    double lineStartLongitude = iteratingLongitude;
                    double lineEndLongitude = iteratingLongitude + 10;
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
        List<Integer>[][] coastlineIntersectionsLongitude = new List[18][36];
        listLatIdx = 0;
        for (double iteratingLatitude = -90.0; iteratingLatitude <= 80; iteratingLatitude += 10) {
            int listLongIdx = 0;
            for (double iteratingLongitude = -180.0; iteratingLongitude <= 170; iteratingLongitude += 10) {
                System.out.println("ttt: loop 2");
                //todo: check if indices of coastlineIntersectionsLatitude are given correctly
                coastlineIntersectionsLongitude[listLatIdx][listLongIdx] = new ArrayList<Integer>();
                System.out.println("ttt: number of coastlines: " + numberOfCoastlines);
                for (int i = 0; i < numberOfCoastlines; i++) {
                    double lineStartLatitude = iteratingLatitude;
                    double lineEndLatitude = iteratingLatitude + 10;
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
        for (double lowLatBound = -90.0; lowLatBound <= 80; lowLatBound += 10) {
            double highLatBound = lowLatBound + 10.0;
            int arrayLongIdx = 0;
            for (double lowLongBound = -180.0; lowLongBound <= 170; lowLongBound += 10) {
                double highLongBound = lowLongBound + 10.0;
                List<Integer> coastlinesInArea = new ArrayList<>();
                System.out.println("test: "+lowLongBound + " " + arrayLongIdx + " lat:" + lowLatBound+" "+arrayLatIdx);
                //add coastlines by intersection with longitudinal lines
                coastlinesInArea.addAll(coastlineIntersectionsLongitude[arrayLatIdx][arrayLongIdx]);
                if(arrayLongIdx == 35) {
                    coastlinesInArea.addAll(coastlineIntersectionsLongitude[arrayLatIdx][0]);
                } else {
                    coastlinesInArea.addAll(coastlineIntersectionsLongitude[arrayLatIdx][arrayLongIdx + 1]);
                }
                //add coastlines by intersection with latitudinal lines
                if(arrayLatIdx == 0) {
                    //only north of area relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx][arrayLongIdx]);
                } else if(arrayLatIdx == 17){
                    //only south of area relevant
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx - 1][arrayLongIdx]);
                } else {
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx - 1][arrayLongIdx]);
                    coastlinesInArea.addAll(coastlineIntersectionsLatitude[arrayLatIdx][arrayLongIdx]);
                }

                for (int i = 0; i < numberOfCoastlines; i++) {
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
                    topLevelGrid[arrayLatIdx][arrayLongIdx] = new CoastlineGridLeaf(refLat, refLong, coastlinesInArea);
                    //todo: at some point, we have to initialize the low level grid. not sure if here or later on better
                } else {
                    topLevelGrid[arrayLatIdx][arrayLongIdx] = new CoastlineGridNode(0, lowLatBound,
                            lowLongBound, coastlinesInArea);
                }
                arrayLongIdx++;
            }
            arrayLatIdx++;
        }
    }
}
