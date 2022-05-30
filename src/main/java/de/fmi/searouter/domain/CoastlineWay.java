package de.fmi.searouter.domain;

import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
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


    public CoastlineWay(com.wolt.osm.parallelpbf.entity.Way wayToTransform) {
        this.id = wayToTransform.getId();
        points = new ArrayList<>();
    }

    public CoastlineWay(long id, List<Point> points) {
        this.id = id;
        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
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

    public long getStartNodeID() {
        return this.points.get(0).getId();
    }

    public long getEndNodeID() {
        return this.points.get(this.points.size() - 1).getId();
    }

    /**
     * @param otherCoastlineToCheck The {@link CoastlineWay} to compare with this coastline.
     * @return True if the two coastlines share the same starting node (at position 0 in list), else false.
     */
    private boolean hasSameStartNode(CoastlineWay otherCoastlineToCheck) {
        return this.getStartNodeID() == otherCoastlineToCheck.getStartNodeID();
    }

    /**
     * @param otherCoastlineToCheck The {@link CoastlineWay} to compare with this coastline.
     * @return True if the two coastlines share the same ending node (at position list.size() in list), else false
     */
    private boolean hasSameEndNode(CoastlineWay otherCoastlineToCheck) {
        return this.getEndNodeID() == otherCoastlineToCheck.getEndNodeID();
    }


    /**
     * @param otherCoastlineToCheck The {@link CoastlineWay} to compare with this coastline.
     * @return True if the two coastlines touch while this objects end node and the others
     * object start node fit, else false
     */
    private boolean endNodeIsOthersStartNode(CoastlineWay otherCoastlineToCheck) {
        return this.getEndNodeID() == otherCoastlineToCheck.getStartNodeID();
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

    /**
     * Merges two {@link CoastlineWay}s if the ending points match exactly (polygon case: start1=start2, end1=end2)
     * or the end point of this CoastlineWay is the start point of the coastlineToMergeWith (extended line case: end1=start2).
     *
     * @param coastlineToMergeWith The other coastline which should be merged with the current, if possible.
     * @return coastlineWay The resulting CoastlineWay after a successful merge, or null else.
     */
    public CoastlineWay mergeCoastlinesIfPossible(CoastlineWay coastlineToMergeWith) {

        if (hasSameStartNode(coastlineToMergeWith) && hasSameEndNode(coastlineToMergeWith)) {

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
        }

        if (endNodeIsOthersStartNode(coastlineToMergeWith)) {
            // Attach both lines
            logger.info("Merge coast lines " + this.getId() + " and " + coastlineToMergeWith.getId());

            // Begin by adding the nodes of this CoastlineWay to the new list
            List<Point> newWayNodesOrder = new ArrayList<>(this.points);

            // Add the other coastline by concatenating its nodes in reverse order and skipping the first one (the end node)
            List<Point> wayNodesOfSecondCoastline = coastlineToMergeWith.getPoints();
            for (int i = 1; i < wayNodesOfSecondCoastline.size(); i++) {
                newWayNodesOrder.add(wayNodesOfSecondCoastline.get(i));
            }

            // Return a new CoastlineWay object with the metadata (id, user, ...) of this object
            return new CoastlineWay(this.id, newWayNodesOrder);
        }

        return null;
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

