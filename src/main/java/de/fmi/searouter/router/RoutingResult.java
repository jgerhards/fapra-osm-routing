package de.fmi.searouter.router;

import de.fmi.searouter.dijkstragrid.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * class storing the result of a routing request. Used for communicating with the REST API
 */
public class RoutingResult {

    private List<Integer> path;
    private List<List<Double>> pathCoordinates;
    private int overallDistance;
    private double calculationTimeInMs;

    public RoutingResult(List<Integer> path, int overallDistance, double calculationTimeInMs) {
        this.setPath(path);
        this.overallDistance = overallDistance;
        this.calculationTimeInMs = calculationTimeInMs;
    }

    public RoutingResult(List<Integer> path, int overallDistance) {
        this.setPath(path);
        this.overallDistance = overallDistance;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
        this.pathCoordinates = new ArrayList<>();
        for (Integer nodeIdx : path) {
            List<Double> coord = new ArrayList<>();
            coord.add(Node.getLatitude(nodeIdx));
            coord.add(Node.getLongitude(nodeIdx));
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
