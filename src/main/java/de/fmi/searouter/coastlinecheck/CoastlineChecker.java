package de.fmi.searouter.coastlinecheck;

import de.fmi.searouter.domain.IntersectionHelper;

import java.util.ArrayList;
import java.util.List;

public class CoastlineChecker {

    //grid containing the top level of CoastlineGridElements. index depends on coordinates rounded up to the next 10
    private CoastlineGridElement[][] topLevelGrid;

    CoastlineChecker() {
        topLevelGrid = new CoastlineGridElement[18][36];
        int numberOfCoastlines = Coastlines.getNumberOfWays();

        //create grid (may be stored on hard drive and imported from there later on)
        //first, get coastline intersections with lines along the latitude (constant latitude)
        //latitude has one dimension less as there are only 17 separating lines
        List<Integer>[][] coastlineIntersectionsLatitude = new List[17][36];
        int listLatIdx = 0;

        for(double iteratingLatitude = -80.0; iteratingLatitude <= 80; iteratingLatitude += 10) {
            int listLongIdx = 0;
            for(double iteratingLongitude = -180.0; iteratingLongitude <= 180; iteratingLongitude += 10) {
                //todo: check if indices of coastlineIntersectionsLatitude are given correctly
                coastlineIntersectionsLatitude[listLatIdx][listLongIdx] = new ArrayList<Integer>();
                for(int i = 0; i < numberOfCoastlines; i++) {
                    double lineStartLongitude = iteratingLongitude;
                    double lineEndLongitude = iteratingLongitude + 10;
                    //fix overflow of lineEndLongitude
                    if(lineEndLongitude == 190) {
                        lineEndLongitude = -180.0;
                    }
                    double lineLatitude = iteratingLatitude;

                    double coastLineStartLatitude = Coastlines.getStartLatitude(i);
                    double coastLineStartLongitude = Coastlines.getStartLongitude(i);
                    double coastLineEndLatitude = Coastlines.getEndLatitude(i);
                    double coastLineEndLongitude = Coastlines.getEndLongitude(i);

                    boolean coastlineIntersects = IntersectionHelper.linesIntersect(coastLineStartLatitude,
                            coastLineStartLongitude, coastLineEndLatitude, coastLineEndLongitude, lineLatitude,
                            lineStartLongitude, lineLatitude, lineEndLongitude);
                    if(coastlineIntersects) {
                        coastlineIntersectionsLatitude[listLatIdx][listLongIdx].add(i);
                    }
                }
                listLongIdx++;
            }
            listLatIdx++;
        }

        //now, calculate the same thing for intersection with lines along the longitude
        List<Integer>[][] coastlineIntersectionsLongitude = new List[18][36];
    }
}
