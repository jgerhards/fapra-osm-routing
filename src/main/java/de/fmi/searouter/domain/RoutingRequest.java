package de.fmi.searouter.domain;

public class RoutingRequest {

    private LatLong startPoint;
    private LatLong endPoint;

    public LatLong getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(LatLong startPoint) {
        this.startPoint = startPoint;
    }

    public LatLong getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLong endPoint) {
        this.endPoint = endPoint;
    }
}
