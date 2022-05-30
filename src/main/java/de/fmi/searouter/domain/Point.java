package de.fmi.searouter.domain;

public class Point {

    private final float lat;
    private final float lon;
    private final long id;

    public Point(long id, float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
        this.id = id;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public long getId() {
        return id;
    }
}
