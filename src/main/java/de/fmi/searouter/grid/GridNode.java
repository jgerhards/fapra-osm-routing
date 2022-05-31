package de.fmi.searouter.grid;

import java.math.BigDecimal;

/**
 * This is an temporary object used for building up the final adjaceny array structure
 * when self-generating the graph grid structure.
 */
public class GridNode {

    private double latitude;
    private double longitude;

    private int id;

    public GridNode(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GridNode calcNorthernNode(double latitudeOffset) {
        BigDecimal offset = BigDecimal.valueOf(latitudeOffset);

        BigDecimal nLatitude = BigDecimal.valueOf(latitude).add(offset);

        if (nLatitude.doubleValue() > 90.0 || nLatitude.doubleValue() < -90.0) {
            return null;
        }

        return new GridNode(nLatitude.doubleValue(), longitude);
    }

    public GridNode calcSouthernNode(double latitudeOffset) {
        BigDecimal offset = BigDecimal.valueOf(latitudeOffset);

        BigDecimal nLatitude = BigDecimal.valueOf(latitude).subtract(offset);

        if (nLatitude.doubleValue() > 90.0 || nLatitude.doubleValue() < -90.0) {
            return null;
        }

        return new GridNode(nLatitude.doubleValue(), longitude);
    }

    public GridNode calcEasternNode(double longitudeOffset) {
        BigDecimal offset = BigDecimal.valueOf(longitudeOffset);

        BigDecimal nLongitude = BigDecimal.valueOf(longitude).add(offset);

        if (nLongitude.doubleValue() < -180.0) {
           // nLongitude = nLongitude % 180;
           nLongitude = nLongitude.remainder(BigDecimal.valueOf(180));
        } else if (nLongitude.doubleValue() > 180) {
            nLongitude = BigDecimal.valueOf(-180).add(nLongitude.remainder(BigDecimal.valueOf(180)));
        }

        return new GridNode(latitude, nLongitude.doubleValue());
    }

    public GridNode calcWesternNode(double longitudeOffset) {
        BigDecimal offset = BigDecimal.valueOf(longitudeOffset);

        BigDecimal nLongitude = BigDecimal.valueOf(longitude).subtract(offset);

        if (nLongitude.doubleValue() < -180.0) {
            // nLongitude = nLongitude % 180;
            nLongitude = nLongitude.remainder(BigDecimal.valueOf(180));
        } else if (nLongitude.doubleValue() > 180) {
            nLongitude = BigDecimal.valueOf(-180).add(nLongitude.remainder(BigDecimal.valueOf(180)));
        }

        return new GridNode(latitude, nLongitude.doubleValue());
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
