package de.fmi.searouter.domain;

import de.fmi.searouter.utils.IntersectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a coastline way and after the import a coastline polygon. This object
 * is only used for the import mechanism and will be transformed into a
 * more efficient data structure after the import process took place.
 */
public class CoastlineWay {

    private Logger logger = LoggerFactory.getLogger(CoastlineWay.class);

    // All coordinate points of this coastline way section.
    private List<Point> points;


    public int getNumberOfEdges() {
        if (points.size() <= 1) {
            return 0;
        }
        return points.size() - 1;
    }

    public int getNumberOfPoints() {
        if (points.size() <= 1) {
            return 0;
        }

        return points.size();
    }

    public CoastlineWay(com.wolt.osm.parallelpbf.entity.Way wayToTransform) {
        points = new ArrayList<>();
    }

    public CoastlineWay(List<Point> points) {

        this.points = points;

    }

    public List<Point> getPoints() {
        return points;
    }



    public void setPoints(List<Point> points) {
        this.points = points;
    }
}

