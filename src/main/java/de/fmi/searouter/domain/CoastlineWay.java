package de.fmi.searouter.domain;

import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a coastline represented internally as osm way. This object
 * is only used for the import mechanism and will be transformed into a
 * more efficient data structure after the import process took place.
 */
public class CoastlineWay extends Way {

    private Logger logger = LoggerFactory.getLogger(CoastlineWay.class);

    public CoastlineWay(Way wayToTransform) {
        super(new CommonEntityData(
                        wayToTransform.getId(),
                        wayToTransform.getVersion(),
                        wayToTransform.getTimestamp(),
                        wayToTransform.getUser(),
                        wayToTransform.getChangesetId()),
                wayToTransform.getWayNodes());
    }

    /**
     * Overwrites the current id with a new one.
     *
     * @param id The new id for this {@link CoastlineWay}
     */
    public void setId(long id) {
        super.setId(id);
    }

    public CoastlineWay(CommonEntityData entityData) {
        super(entityData);
    }

    public CoastlineWay(CommonEntityData entityData, List<WayNode> wayNodes) {
        super(entityData, wayNodes);
    }

    public CoastlineWay(StoreReader sr, StoreClassRegister scr) {
        super(sr, scr);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public long getStartNodeID() {
        return this.getWayNodes().get(0).getNodeId();
    }

    public long getEndNodeID() {
        return this.getWayNodes().get(this.getWayNodes().size() - 1).getNodeId();
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
            List<WayNode> newWayNodesOrder = new ArrayList<>(this.getWayNodes());

            // Add the other coastline by concatenating its nodes in reverse order and skipping the first one (the end node)
            List<WayNode> wayNodesOfSecondCoastline = coastlineToMergeWith.getWayNodes();
            for (int i = wayNodesOfSecondCoastline.size() - 2; i >= 0; i--) {
                newWayNodesOrder.add(wayNodesOfSecondCoastline.get(i));
            }

            // Return a new CoastlineWay object with the metadata (id, user, ...) of this object
            CommonEntityData newEntityData = new CommonEntityData(this.getId(), this.getVersion(), this.getTimestamp(), this.getUser(), this.getChangesetId());
            return new CoastlineWay(newEntityData, newWayNodesOrder);
        }

        if (endNodeIsOthersStartNode(coastlineToMergeWith)) {
            // Attach both lines
            logger.info("Merge coast lines " + this.getId() + " and " + coastlineToMergeWith.getId());

            // Begin by adding the nodes of this CoastlineWay to the new list
            List<WayNode> newWayNodesOrder = new ArrayList<>(this.getWayNodes());

            // Add the other coastline by concatenating its nodes in reverse order and skipping the first one (the end node)
            List<WayNode> wayNodesOfSecondCoastline = coastlineToMergeWith.getWayNodes();
            for (int i = 1; i < wayNodesOfSecondCoastline.size(); i++) {
                newWayNodesOrder.add(wayNodesOfSecondCoastline.get(i));
            }

            // Return a new CoastlineWay object with the metadata (id, user, ...) of this object
            CommonEntityData newEntityData = new CommonEntityData(this.getId(), this.getVersion(), this.getTimestamp(), this.getUser(), this.getChangesetId());
            return new CoastlineWay(newEntityData, newWayNodesOrder);
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

        for (int i = 1; i < this.getWayNodes().size(); i++) {
            WayNode startNode = this.getWayNodes().get(i - 1);
            WayNode destinationNode = this.getWayNodes().get(i);

            // Cumulate the length of the current edge looked at
            length += IntersectionHelper.getDistance(
                    startNode.getLatitude(), startNode.getLongitude(),
                    destinationNode.getLatitude(), destinationNode.getLatitude()
            );
        }

        return length;
    }
}

