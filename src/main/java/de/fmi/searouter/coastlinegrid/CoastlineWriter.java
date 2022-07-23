package de.fmi.searouter.coastlinegrid;

import java.io.Serializable;

class CoastlineWriter implements Serializable {
    private int[] edgePosStart;

    private float[] pointLon;
    private float[] pointLat;

    public int[] getEdgePosStart() {
        return edgePosStart;
    }

    public float[] getPointLon() {
        return pointLon;
    }

    public float[] getPointLat() {
        return pointLat;
    }

    CoastlineWriter(int[] edgePosStart, float[] pointLat, float[] pointLon) {
        this.edgePosStart = edgePosStart;
        this.pointLat = pointLat;
        this.pointLon = pointLon;
    }
}
