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

    //true if a route was found, else false
    private boolean routeFound;

    //indicates which router is used, this is relevant when determining the corrdinates of nodes on the path
    private static boolean useHubLabelRouter;

    /**
     * Set which router will be used to generate results
     * @param useHubLabelRouter true if the hub label router is used, else false
     */
    public static void setHLRouterUse(boolean useHubLabelRouter) {
        RoutingResult.useHubLabelRouter = useHubLabelRouter;
    }

    /**
     * Check if a route was found.
     * @return true if a route was found, else false
     */
    public boolean routeFound() {
        return routeFound;
    }

    /**
     * Constructor. Set fields for the path, the distance and the calculation time. Use only if
     * a path was found.
     * @param path the path
     * @param overallDistance the distance of the path
     * @param calculationTimeInMs the calculation time
     */
    public RoutingResult(List<Integer> path, int overallDistance, double calculationTimeInMs) {
        this.setPath(path);
        this.overallDistance = overallDistance;
        this.calculationTimeInMs = calculationTimeInMs;
        this.routeFound = true;
    }

    /**
     * Constructor. Includes setting fields for the calculation time and indication if a route was found.
     * @param calculationTimeInMs the calculation time
     * @param routeFound true if a path was found, else false
     */
    public RoutingResult(double calculationTimeInMs, boolean routeFound) {
        this.calculationTimeInMs = calculationTimeInMs;
        this.routeFound = routeFound;
    }

    /**
     * Constructor. Creates mostly empty result which does not contain a path. Only sets indication that no
     * route was found.
     */
    public RoutingResult() {
        this.routeFound = false;
    }

    /**
     * Constructor. Includes setting fields for the path, overall distance and the indicator whether a route was found.
     * @param path the path
     * @param overallDistance the distance of the path
     * @param routeFound true if a route was found, else false
     */
    public RoutingResult(List<Integer> path, int overallDistance, boolean routeFound) {
        this.setPath(path);
        this.overallDistance = overallDistance;
        this.routeFound = routeFound;
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

    @Override
    public String toString() {
        return "RoutingResult{" +
                "path=" + path +
                ", overallDistance=" + overallDistance +
                ", calculationTimeInMs=" + calculationTimeInMs +
                '}';
    }

    //getters and setters

    public List<Integer> getPath() {
        return path;
    }

    public int getOverallDistance() {
        return overallDistance;
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
}
