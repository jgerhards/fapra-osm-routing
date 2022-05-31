package de.fmi.searouter.grid;

import com.google.common.math.DoubleMath;
import de.fmi.searouter.domain.CoastlineWay;

/**
 * Implementation of the point-in-polygon test of Bevis in Chatelain as described in
 * "Locating a Point on a Spherical Surface Relative to a
 * Spherical Polygon of Arbitrary Shape" (Mathematical Geology, Vol. 21, No. 8, 1989).
 * Transcribed from fortran to Java.
 */
public class BevisChatelainInPolygonCheck {

    // Delta for double comparisons
    double epsilon = 0.000001d;

    double[] vlat_c;
    double[] vlon_c;
    int nv_c;
    double xlat_c;
    double xlon_c;
    double[] tlonv;
    int ibndry;

    /**
     * Inits all data points for one polygon
     * @param polygonToCheck
     */
    public BevisChatelainInPolygonCheck(CoastlineWay polygonToCheck) {

        double lats[] = polygonToCheck.getLatitudeArray();
        double longs[] = polygonToCheck.getLongitudeArray();

        // Pass the point which acts as reference point for a water node
        DefSPolyBndry(lats, longs, 83.15311098437887, 23.90625);
    }

    /**
     * @param lat The latitude of the point to check
     * @param longitude The longitude of the point to check
     * @return True if point is outside the polygon --> in water
     */
    public boolean isPointInWater(double lat, double longitude) {
        int res = LetPtRelBndry(lat, longitude);
        return res == 1 || res == 2;
    }

    private double dsin(double x) {
        return Math.sin(x);
    }

    private double dcos(double x) {
        return Math.cos(x);
    }

    private double datan2(double y, double x) {
        return Math.atan2(y, x);
    }

    /**
     * Inits the array data structures for preparing polygon test calculations.
     */
    public void DefSPolyBndry(double[] vlat, double[] vlon, double xlat, double xlon) {
        ibndry = 1;

        if (vlat.length != vlon.length) {
            System.out.println("vlat and vlon arrays do not have same length!");
            return;
        }

        vlat_c = new double[vlat.length];
        vlon_c = new double[vlon.length];

        nv_c = vlat.length;
        xlat_c = xlat;
        xlon_c = xlon;

        // Transformed longitude (for acting as a north pole)
        tlonv = new double[vlat.length];

        int ip = 0;

        for (int i = 0; i < nv_c; i++) {
            vlat_c[i] = vlat[i];
            vlon_c[i] = vlon[i];
            tlonv[i] = TransfmLon(xlat, xlon, vlat[i], vlon[i]);


            // Check error cases
            /*
            if (i == nv_c-1) {
                ip = 1;
            } else {
                ip = i + 1;
            }

            if (DoubleMath.fuzzyEquals(vlat[i], vlat[ip], epsilon) && DoubleMath.fuzzyEquals(vlon[i], vlon[ip], epsilon)) {
                System.out.println("DefSPolyBndry detects user error: vertices i and ip are not distinct");
                return;
            }

            if (DoubleMath.fuzzyEquals(tlonv[i], tlonv[ip], epsilon)) {
                System.out.println("DefSPolyBndry detects user error: vertices ',i,' & ',ip,' on same gt. circle as X");
                return;
            }

            if (DoubleMath.fuzzyEquals(vlat[i], vlat[ip], epsilon)) {
                double dellon = vlon[i] - vlon[ip];
                if (dellon > +180d) dellon = dellon -360;
                if (dellon < -180d) dellon = dellon + 360;
                if (DoubleMath.fuzzyEquals(dellon, 180d, epsilon) || DoubleMath.fuzzyEquals(dellon, -180d, epsilon)) {
                    System.out.println("DefSPolyBndry detects user error: vertices i and ip are antipodal");
                    return;
                }
            }
            */
        }
    }

    /**
     * Checks whether a point P with latitude plat and longitude plon is inside, on a side or outside the polygon-
     * @param plat Latitude of point P
     * @param plon Longitude of point P
     * @return 0: P outside polygon, 1: P inside polygon, 2: P is on polygon boundary ; 3: user error
     */
    public int LetPtRelBndry(double plat, double plon) {
        double dellon;

        if (this.ibndry == 0) {
            // user never defined bndry
            System.out.println("Subroutine LctPtRelBndry detects user error: Subroutine DefSPolyBndry must be called before subroutine LctPtRelBndry can be called");
            return 3;
        }

        if (DoubleMath.fuzzyEquals(plat, -xlat_c, epsilon)) {
            dellon = plon - xlon_c;
            if (dellon < -180d) dellon = dellon + 360d;
            if (dellon > 180d) dellon = dellon - 360d;
            if (DoubleMath.fuzzyEquals(dellon, 180d, epsilon) || DoubleMath.fuzzyEquals(dellon, -180d, epsilon)) {
                System.out.println("Warning: LctPtRelBndry detects case P antipodal to X location of P relative to S is undetermined");
                return 3;
            }
        }

        int icross = 0; // init counter

        if (DoubleMath.fuzzyEquals(plat, xlat_c, epsilon) && DoubleMath.fuzzyEquals(plon, xlon_c, epsilon)) {
            return 1;
        }

        double tlonP = TransfmLon(xlat_c, xlon_c, plat, plon);

        for (int i = 0; i < nv_c - 1; i++) { // Loop over sides of S nv_c = array size

            double vAlat = vlat_c[i];
            double vAlon = vlon_c[i];
            double tlonA = tlonv[i];
            double vBlat, vBlon, tlonB;

            vBlat = vlat_c[i + 1];
            vBlon = vlon_c[i + 1];
            tlonB = tlonv[i + 1];

            int istrike = 0;

            if (DoubleMath.fuzzyEquals(tlonP, tlonA, epsilon)) {
                istrike = 1;
            } else {
                int ibrngAB = EastOrWest(tlonA, tlonB);
                int ibrngAP = EastOrWest(tlonA, tlonP);
                int ibrngPB = EastOrWest(tlonP, tlonB);

                if (ibrngAP == ibrngAB && ibrngPB == ibrngAB) {
                    istrike = 1;
                }
            }

            if (istrike == 1) {
                if (DoubleMath.fuzzyEquals(plat, vAlat, epsilon) && DoubleMath.fuzzyEquals(plon, vAlon, epsilon)) {
                    return 2; // P lies on a vertex of S
                }


                double tlon_X = TransfmLon(vAlat, vAlon, xlat_c, xlon_c);
                double tlon_B = TransfmLon(vAlat, vAlon, vBlat, vBlon);
                double tlon_P = TransfmLon(vAlat, vAlon, plat, plon);

                if (DoubleMath.fuzzyEquals(tlon_P, tlon_B, epsilon)) {
                    return 2; // P lies on side of S
                } else {
                    int ibrng_BX = EastOrWest(tlon_B, tlon_X);
                    int ibrng_BP = EastOrWest(tlon_B, tlon_P);
                    if (ibrng_BX == (-ibrng_BP)) icross = icross + 1;
                }
            }
        }

        if (icross % 2 == 0) {
            return 1;
        }

        // default P is outside of S
        return 0;
    }

    /**
     * Finds the longitude f point Q in a geographic coordinate system for which point P acts as a 'north
     * pole.

     * @return transformed longitude in degrees
     */
    public double TransfmLon(double plat, double plon, double qlat, double qlon) {
        double t, b;

        if (DoubleMath.fuzzyEquals(plat, 90d, epsilon)) {
            return qlon;
        } else {
            t = dsin(Math.toRadians((qlon - plon))) * dcos(Math.toRadians(qlat));
            b = dsin(Math.toRadians(qlat)) * dcos(Math.toRadians(plat)) - dcos(Math.toRadians(qlat)) * dsin(Math.toRadians(plat)) * dcos(Math.toRadians(qlon - plon));
            return Math.toDegrees(datan2(t, b));
        }
    }

    /**
     * Determines whether D lies west or east of C by traveling the shortest path.
     *
     * @param clon longitude of point C
     * @param dlon longitude of point D
     * @return 1: D is east of C, -1: D is west of C, 0: D north or south of C
     */
    public int EastOrWest(double clon, double dlon) {
        double del = dlon - clon;

        if (del > 180) del = del - 360;
        if (del < -180) del = del + 360;
        if (del > 0.0 && !DoubleMath.fuzzyEquals(del, +180d, epsilon)) {
            return +1; // D is east of C
        } else if (del < 0.0 && !DoubleMath.fuzzyEquals(del, -180d, epsilon)) {
            return -1; // D is west of C
        } else {
            return 0; //D north or south of C
        }
    }
}
