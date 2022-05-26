package de.fmi.searouter.router;

import java.util.List;

public class RoutingResult {

    private List<Integer> path;
    private int overallDistance;
    private double calculationTimeInMs;

    public RoutingResult(List<Integer> path, int overallDistance, double calculationTimeInMs) {
        this.path = path;
        this.overallDistance = overallDistance;
        this.calculationTimeInMs = calculationTimeInMs;
    }

    public RoutingResult(List<Integer> path, int overallDistance) {
        this.path = path;
        this.overallDistance = overallDistance;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
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

    @Override
    public String toString() {
        return "RoutingResult{" +
                "path=" + path +
                ", overallDistance=" + overallDistance +
                ", calculationTimeInMs=" + calculationTimeInMs +
                '}';
    }
}
