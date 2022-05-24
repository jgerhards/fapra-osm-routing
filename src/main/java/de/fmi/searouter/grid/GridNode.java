package de.fmi.searouter.grid;

public class GridNode {

    private double latitude;
    private double longitude;

    private int id;

    public GridNode(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GridNode calcNorthernNode(double latitudeOffset) {
        double nLatitude = latitude + latitudeOffset;

        if (nLatitude > 90.0) {
            return null;
        }

        return new GridNode(nLatitude, longitude);
    }

    public GridNode calcSouthernNode(double latitudeOffset) {
        double nLatitude = latitude - latitudeOffset;

        if (nLatitude < -90.0) {
            return null;
        }

        return new GridNode(nLatitude, longitude);
    }

    public GridNode calcEasternNode(double longitudeOffset) {
        double nLongitude = longitude + longitudeOffset;

        if (nLongitude < -180.0) {
            nLongitude = nLongitude % 180;
        } else if (nLongitude > 180) {
            nLongitude = nLongitude % (-180);
        }

        return new GridNode(latitude, nLongitude);
    }

    public GridNode calcWesternNode(double longitudeOffset) {
        double nLongitude = longitude - longitudeOffset;

        if (nLongitude < -180.0) {
            nLongitude = nLongitude % 180;
        } else if (nLongitude > 180) {
            nLongitude = nLongitude % (-180);
        }

        return new GridNode(latitude, nLongitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
