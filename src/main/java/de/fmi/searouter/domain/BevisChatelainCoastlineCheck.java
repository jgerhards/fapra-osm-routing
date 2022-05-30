package de.fmi.searouter.domain;

import com.google.common.math.DoubleMath;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.math.BigDecimal;
import java.util.List;

/**
 * Analogeous implementation of the point-in-polygon test of Bevis in Chatelain in
 * "Locating a Point on a Spherical Surface Relative to a
 * Spherical Polygon of Arbitrary Shape" (Mathematical Geology, Vol. 21, No. 8, 1989).
 */
public class BevisChatelainCoastlineCheck {

    double epsilon = 0.000001d;

    double[] vlat_c;
    double[] vlon_c;
    int nv_c;
    double xlat_c;
    double xlon_c;
    double[] tlonv;
    int ibndry;

    public BevisChatelainCoastlineCheck(CoastlineWay polygonToCheck) {
        List<Point> wayNodes = polygonToCheck.getPoints();

        double lats[] = new double[wayNodes.size()];
        double longs[] = new double[wayNodes.size()];

        for (int i = 0; i < wayNodes.size(); i++) {
            lats[i] = wayNodes.get(i).getLat();
            longs[i] = wayNodes.get(i).getLon();
        }


        //DefSPolyBndry(lats, longs, 90.0, 0.0);
        //DefSPolyBndry(lats, longs, 	-89.99996, 	0.0001);
        DefSPolyBndry(lats, longs, 83.15311098437887, 23.90625);
        //DefSPolyBndry(lats, longs, 	-62.2328, 		-58.4599);
        //DefSPolyBndry(lats, longs, 		-62.2327, 	-58.4603);
        //DefSPolyBndry(lats, longs, 		-75.4255, 	-42.7808);
    }

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

        tlonv = new double[vlat.length];

        int ip = 0;

        for (int i = 0; i < nv_c; i++) {
            vlat_c[i] = vlat[i];
            vlon_c[i] = vlon[i];
            tlonv[i] = TransfmLon(xlat, xlon, vlat[i], vlon[i]);

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
                if (dellon < -180d) dellon = dellon + 360; // TODO changed from - to +
                if (DoubleMath.fuzzyEquals(dellon, 180d, epsilon) || DoubleMath.fuzzyEquals(dellon, -180d, epsilon)) {
                    System.out.println("DefSPolyBndry detects user error: vertices i and ip are antipodal");
                    return;
                }
            }
            */


        }


    }

    public int LetPtRelBndry(double plat, double plon) {
        int mxnv;
        double dellon;

        if (this.ibndry == 0) {
            // user never defined bndry
            System.out.println("Subroutine LctPtRelBndry detects user error: Subroutine DefSPolyBndry must be called before subroutine LctPtRelBndry can be called");
            return 0;
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
