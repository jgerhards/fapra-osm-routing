package de.fmi.searouter.domain;

import de.fmi.searouter.utils.IntersectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a coastline represented internally as osm way. This object
 * is only used for the import mechanism and will be transformed into a
 * more efficient data structure after the import process took place.
 */
public class CoastlineWay {

    private Logger logger = LoggerFactory.getLogger(CoastlineWay.class);

    private List<Point> points;
    private long id;

    private long startNodeId;
    private long endNodeId;

    private int pointSize;

    private CoastlineWay nextCoastlineWay;
    private CoastlineWay lastCoastlineWay;

    public CoastlineWay(com.wolt.osm.parallelpbf.entity.Way wayToTransform) {
        this.id = wayToTransform.getId();
        points = new ArrayList<>();
        startNodeId = -1;
        endNodeId = -1;
        nextCoastlineWay = null;
        lastCoastlineWay = null;
        pointSize = 0;
    }

    public CoastlineWay(long id, List<Point> points) {
        this.id = id;
        this.points = points;
        startNodeId = calcStartNodeId();
        endNodeId = calcEndNodeId();
        pointSize = points.size();
        nextCoastlineWay = null;
        lastCoastlineWay = null;
    }

    public void updatePointSize() {
        this.pointSize = points.size();
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

    public double[] getLongitudeArray() {

        double[] longitude = new double[this.pointSize];

        for (int i = 0; i < this.points.size(); i++) {
            longitude[i] = this.points.get(i).getLon();
        }
        CoastlineWay iterator = nextCoastlineWay;
        int index = this.points.size();
        while (iterator != null) {
            index = iterator.getLongitudeArrayRecursive(index, longitude);
            iterator = iterator.getNextCoastlineWay();
        }

        return longitude;
    }

    private int getLongitudeArrayRecursive(int startIndex, double[] longitude) {
        for (int i = 0; i < this.points.size(); i++) {
            longitude[i+startIndex] = this.points.get(i).getLon();
        }

        return startIndex + this.points.size();
    }

    public double[] getLatitudeArray() {

        double[] latitude = new double[this.pointSize];

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
        pointSize = points.size();
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


    /**
     * @param otherCoastlineToCheck The {@link CoastlineWay} to compare with this coastline.
     * @return True if the two coastlines touch while this objects end node and the others
     * object start node fit, else false
     */
    private boolean endNodeIsOthersStartNode(CoastlineWay otherCoastlineToCheck) {
        return this.calcEndNodeId() == otherCoastlineToCheck.calcStartNodeId();
    }

    /**
     * Checks whether this {@link CoastlineWay} is mergeable with another coastline under the
     * assumption that this CoastlineWay is the starting point of a bigger way.
     *
     * @param otherCoastlineToCheck The {@link CoastlineWay} to compare with this coastline.
     * @return True if the coastline is mergeable
     */
    private boolean isMergeableWithCoastline(CoastlineWay otherCoastlineToCheck) {
        return endNodeIsOthersStartNode(otherCoastlineToCheck) || (hasSameStartNode(otherCoastlineToCheck) && hasSameEndNode(otherCoastlineToCheck));
    }

    public int getPointSize() {
        return pointSize;
    }

    public void appendCoastlineWay(CoastlineWay way) {
        // Sanity check
        if (way.getPointSize() == 0) {
            System.out.println("ERROR: Coastline Way with empty list");
            System.out.println(way.getPoints().size());
            System.exit(1);
            return;
        }

        this.endNodeId = way.getEndNodeId();
        this.pointSize = this.pointSize - 1 + way.getPointSize();

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
     * Merges two {@link CoastlineWay}s if the ending points match exactly (polygon case: start1=start2, end1=end2)
     * or the end point of this CoastlineWay is the start point of the coastlineToMergeWith (extended line case: end1=start2).
     *
     * @param coastlineToMergeWith The other coastline which should be merged with the current, if possible.
     * @return 0: No merge, 1: This obj merged (this beofre coastlineTomergewith), 2: coastlineToMerge before this obj
     */
    public int mergeCoastlinesIfPossible(CoastlineWay coastlineToMergeWith) {

        if (hasSameStartNode(coastlineToMergeWith) && hasSameEndNode(coastlineToMergeWith)) {

            /*
            // Build a polygon if the two ways share start and end node
            logger.info("Merge coast lines " + this.getId() + "and " + coastlineToMergeWith.getId() + " by building a polygon.");
            System.out.println("Will perform merge");

            // Begin by adding the nodes of this CoastlineWay to the new list
            List<Point> newWayNodesOrder = new ArrayList<>(this.points);

            // Add the other coastline by concatenating its nodes in reverse order and skipping the first one (the end node)
            List<Point> wayNodesOfSecondCoastline = coastlineToMergeWith.points;
            for (int i = wayNodesOfSecondCoastline.size() - 2; i >= 0; i--) {
                newWayNodesOrder.add(wayNodesOfSecondCoastline.get(i));
            }

            // Return a new CoastlineWay object with the metadata (id, user, ...) of this object
            return new CoastlineWay(this.id, newWayNodesOrder);
            */

            System.out.println("ERROR: Special case for merging occurred");
            System.exit(1);
        }


        // This end node is others start node
        if (this.endNodeId == coastlineToMergeWith.getStartNodeId()) {
            this.appendCoastlineWay(coastlineToMergeWith);
            logger.info("Merge coast lines " + this.getId() + "and " + coastlineToMergeWith.getId() + " by building a polygon.");

            return 1;
        } else if (this.startNodeId == coastlineToMergeWith.getEndNodeId()) {
            coastlineToMergeWith.appendCoastlineWay(this);
            logger.info("Merge coast lines " + this.getId() + "and " + coastlineToMergeWith.getId() + " by building a polygon.");

            return 2;
        }

        return 0;
    }

    /**
     * Calculates the overall length of this {@link CoastlineWay} (=perimeter of polygon).
     *
     * @return The length (or perimeter) of this CoastlineWay (or coastline polygon)
     */
    public double getLength() {
        double length = 0.;

        for (int i = 1; i < this.points.size(); i++) {
            Point startNode = this.points.get(i - 1);
            Point destinationNode = this.points.get(i);

            // Cumulate the length of the current edge looked at
            length += IntersectionHelper.getDistance(
                    startNode.getLat(), startNode.getLon(),
                    destinationNode.getLat(), destinationNode.getLon()
            );
        }

        return length;
    }
}

