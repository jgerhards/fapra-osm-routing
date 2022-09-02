package de.fmi.searouter.router;

import de.fmi.searouter.dijkstragrid.Node;
import de.fmi.searouter.hublabeldata.HubLNodes;

import java.util.ArrayList;
import java.util.List;

/**
 * class storing the result of a routing request. Used for communicating with the REST API
 */
public class RoutingResult {

    /**
     * All graph nodes ids that make up the resulted path between a start and destination node.
     */
    private List<Integer> path;

    /**
     * Stores coordinates of points. The inner list represents lat-lon pairs representing the coordinates
     * of one point. The outer list stores all these points in the order of the path found.
     * E.g.: [[0.2, 48.1], [24.0, -47.3]]
     */
    private List<List<Double>> pathCoordinates;

    /**
     * The overall distance of the calculated path.
     */
    private int overallDistance;

    /**
     * The time it took to calculate the path.
     */
    private double calculationTimeInMs;

    private boolean routeFound;

    private static boolean useHubLabelRouter;

    public static void setHLRouterUse(boolean useHubLabelRouter) {
        RoutingResult.useHubLabelRouter = useHubLabelRouter;
    }

    public boolean routeFound() {
        return routeFound;
    }

    public RoutingResult(List<Integer> path, int overallDistance, double calculationTimeInMs) {
        this.setPath(path);
        this.overallDistance = overallDistance;
        this.calculationTimeInMs = calculationTimeInMs;
        this.routeFound = true;
    }

    public RoutingResult(double calculationTimeInMs, boolean routeFound) {
        this.calculationTimeInMs = calculationTimeInMs;
        this.routeFound = routeFound;
    }

    public RoutingResult() {
        this.routeFound = false;
    }

    public RoutingResult(List<Integer> path, int overallDistance, boolean routeFound) {
        this.setPath(path);
        this.overallDistance = overallDistance;
        this.routeFound = routeFound;
    }

    public List<Integer> getPath() {
        return path;
    }

    /**
     * Calculates the path coordinates based on a list of IDs of nodes.
     * @param path contains the IDs of all nodes on the path
     */
    public void setPath(List<Integer> path) {
        this.path = path;
        this.pathCoordinates = new ArrayList<>();
        for (Integer nodeIdx : path) {
            List<Double> coord = new ArrayList<>();
            if(!useHubLabelRouter) {
                coord.add(Node.getLatitude(nodeIdx));
                coord.add(Node.getLongitude(nodeIdx));
            } else {
                coord.add(HubLNodes.getLat(nodeIdx));
                coord.add(HubLNodes.getLong(nodeIdx));
            }
            this.pathCoordinates.add(coord);
        }
    }

    public int getOverallDistance() {
        return overallDistance;
    }

    public void setOverallDistance(int overallDistance) {
        this.overallDistance = overallDistance;
    }

    public double getCalculationTimeInMs() {
        return calculationTimeInMs;
    }

    public void setCalculationTimeInMs(double calculationTimeInMs) {
        this.calculationTimeInMs = calculationTimeInMs;
    }

    public List<List<Double>> getPathCoordinates() {
        return pathCoordinates;
    }

    public void setPathCoordinates(List<List<Double>> pathCoordinates) {
        this.pathCoordinates = pathCoordinates;
    }

    @Override
    public String toString() {
        return "RoutingResult{" +
                "path=" + path +
                ", overallDistance=" + overallDistance +
                ", calculationTimeInMs=" + calculationTimeInMs +
                '}';
    }
}
