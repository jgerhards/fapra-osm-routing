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

    private long id;

    private long startNodeId;

    // The end
    private long endNodeId;

    // Number of all nodes in a coastline polygon. Only used for first coastlineway in a series of coastlineways.
    private int polygonLength;

    // The coastline which comes after this CoastlineWay in the polygon
    private CoastlineWay nextCoastlineWay;

    // The coastline which comes before this CoastlineWay in the polygon
    private CoastlineWay lastCoastlineWay;

    public CoastlineWay(com.wolt.osm.parallelpbf.entity.Way wayToTransform) {
        this.id = wayToTransform.getId();
        points = new ArrayList<>();
        startNodeId = -1;
        endNodeId = -1;
        nextCoastlineWay = null;
        lastCoastlineWay = null;
        polygonLength = 0;
    }

    public CoastlineWay(long id, List<Point> points) {
        this.id = id;
        this.points = points;
        startNodeId = calcStartNodeId();
        endNodeId = calcEndNodeId();
        polygonLength = points.size();
        nextCoastlineWay = null;
        lastCoastlineWay = null;

    }

    public void updateAfterGet() {
        this.polygonLength = points.size();
        this.startNodeId = calcStartNodeId();
        this.endNodeId = calcEndNodeId();
    }

    public long getStartNodeId() {
        return startNodeId;
    }

    public long getEndNodeId() {
        return endNodeId;
    }

    public List<Point> getPoints() {
        return points;
    }

    public CoastlineWay getNextCoastlineWay() {
        return nextCoastlineWay;
    }

    public CoastlineWay getLastCoastlineWay() {
        return lastCoastlineWay;
    }

    /**
     * @return The longitudes of the whole CoastlineWay polygon.
     */
    public double[] getLongitudeArray() {

        double[] longitude = new double[this.polygonLength];

        for (int i = 0; i < this.points.size(); i++) {
            longitude[i] = this.points.get(i).getLon();
        }
        CoastlineWay iterator = nextCoastlineWay;
        int index = this.points.size();
        while (iterator != null) {
            index = iterator.getLongitudeArrayInternal(index, longitude);
            iterator = iterator.getNextCoastlineWay();
        }

        return longitude;
    }

    /**
     * @param startIndex Start index for the array fill mechanism
     * @param longitude The longitude array which should be edited.
     * @return The next start index
     */
    private int getLongitudeArrayInternal(int startIndex, double[] longitude) {
        for (int i = 0; i < this.points.size(); i++) {
            longitude[i+startIndex] = this.points.get(i).getLon();
        }

        return startIndex + this.points.size();
    }

    /**
     * @return The latitudes of the whole CoastlineWay polygon.
     */
    public double[] getLatitudeArray() {

        double[] latitude = new double[this.polygonLength];

        for (int i = 0; i < this.points.size(); i++) {
            latitude[i] = this.points.get(i).getLat();
        }
        CoastlineWay iterator = nextCoastlineWay;
        int index = this.points.size();
        while (iterator != null) {
            index = iterator.getLatitudeArrayRecursive(index, latitude);
            iterator = iterator.getNextCoastlineWay();
        }

        return latitude;
    }

    /**
     * @param startIndex Start index for the array fill mechanism
     * @param latitude The longitude array which should be edited.
     * @return The next start index
     */
    private int getLatitudeArrayRecursive(int startIndex, double[] latitude) {
        for (int i = 0; i < this.points.size(); i++) {
            latitude[i+startIndex] = this.points.get(i).getLat();
        }

        return startIndex + this.points.size();
    }

    public void setPoints(List<Point> points) {
        this.points = points;
        startNodeId = calcStartNodeId();
        endNodeId = calcEndNodeId();
        polygonLength = points.size();
    }

    public long getId() {
        return id;
    }

    /**
     * Overwrites the current id with a new one.
     *
     * @param id The new id for this {@link CoastlineWay}
     */
    public void setId(long id) {
        this.id = id;
    }


    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private long calcStartNodeId() {
        return this.points.get(0).getId();
    }

    private long calcEndNodeId() {
        return this.points.get(this.points.size() - 1).getId();
    }

    /**
     * @param otherCoastlineToCheck The {@link CoastlineWay} to compare with this coastline.
     * @return True if the two coastlines share the same starting node (at position 0 in list), else false.
     */
    private boolean hasSameStartNode(CoastlineWay otherCoastlineToCheck) {
        return this.calcStartNodeId() == otherCoastlineToCheck.calcStartNodeId();
    }

    /**
     * @param otherCoastlineToCheck The {@link CoastlineWay} to compare with this coastline.
     * @return True if the two coastlines share the same ending node (at position list.size() in list), else false
     */
    private boolean hasSameEndNode(CoastlineWay otherCoastlineToCheck) {
        return this.calcEndNodeId() == otherCoastlineToCheck.calcEndNodeId();
    }

    public int getPolygonLength() {
        return polygonLength;
    }

    /**
     * Appends a {@link CoastlineWay} to this CoastlineWay by updating the next and last coastlineway references.
     * @param way The coastlineway to append.
     */
    public void appendCoastlineWay(CoastlineWay way) {
        // Sanity check
        if (way.getPolygonLength() == 0) {
            System.out.println("ERROR: Coastline Way with empty list");
            System.out.println(way.getPoints().size());
            System.exit(1);
            return;
        }

        this.endNodeId = way.getEndNodeId();
        this.polygonLength = this.polygonLength - 1 + way.getPolygonLength();

        if (nextCoastlineWay == null) {
            this.lastCoastlineWay = way;
            this.nextCoastlineWay = way;
            this.points.remove(this.points.size() - 1);
        } else {
            this.lastCoastlineWay.appendCoastlineWay(way);
            this.lastCoastlineWay = way;
        }
    }

    /**
     * Merges two {@link CoastlineWay}s if the ending node ids match.
     *
     * @param coastlineToMergeWith The other coastline which should be merged with the current, if possible.
     * @return 0: No merge, 1: Merge with this object being before coastlineToMergeWith in the polygon
     * 2: Merge with coastlineToMergeWith being before this object in the polygon.
     */
    public int mergeCoastlinesIfPossible(CoastlineWay coastlineToMergeWith) {

        if (hasSameStartNode(coastlineToMergeWith) && hasSameEndNode(coastlineToMergeWith)) {

            System.out.println("ERROR: Special case for merging occurred");
            System.exit(1);
        }

        // This end node is others start node
        if (this.endNodeId == coastlineToMergeWith.getStartNodeId()) {
            this.appendCoastlineWay(coastlineToMergeWith);
            logger.info("Cond1: Merge coast lines " + this.getId() + "and " + coastlineToMergeWith.getId() + " by building a polygon.");

            return 1;
        } else if (this.startNodeId == coastlineToMergeWith.getEndNodeId()) {
            coastlineToMergeWith.appendCoastlineWay(this);
            logger.info("Cond2: Merge coast lines " + this.getId() + "and " + coastlineToMergeWith.getId() + " by building a polygon.");

            return 2;
        }

        return 0;
    }
}

