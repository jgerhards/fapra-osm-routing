package de.fmi.searouter.coastlinegrid;

import de.fmi.searouter.importdata.CoastlineWay;
import de.fmi.searouter.importdata.Point;

import java.io.*;
import java.util.List;

public class CoastlineWays {

    // TODO evtl bei double

    private static int[] edgePosStart;

    private static float[] pointLon;
    private static float[] pointLat;

    private static final String filename = "CoastlineWays.ser";

    public static void storeData() {
        CoastlineWriter writer = new CoastlineWriter(edgePosStart, pointLat, pointLon);

        // Serialization
        try
        {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(writer);

            out.close();
            file.close();

            System.out.println("Object has been serialized");

        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
            System.exit(0);
        }
    }

    public static void getData() {
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            CoastlineWriter writer = (CoastlineWriter)in.readObject();

            in.close();
            file.close();

            System.out.println("Object has been deserialized ");
            edgePosStart = writer.getEdgePosStart();
            pointLat = writer.getPointLat();
            pointLon = writer.getPointLon();
       }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
        }

        catch(ClassNotFoundException ex)
        {
            System.out.println("ClassNotFoundException is caught");
        }
    }

    public static void initEdges(List<CoastlineWay> importedCoastlines) {


        int numberOfEdges = 0;
        int numberOfCoordinates = 0;
        for (CoastlineWay currCoastline : importedCoastlines) {
            numberOfEdges += currCoastline.getNumberOfEdges();
            numberOfCoordinates += currCoastline.getNumberOfPoints();
        }

        /*edgePosStart = new int[]{0, 2};
        numberOfEdges = 2;
        pointLat = new float[] {1.2f, 1.3f, 1.2f, 1.3f};
        pointLon = new float[] {2.2f, 2.3f, 2.2f, 2.3f};*/
        edgePosStart = new int[numberOfEdges];

        pointLon = new float[numberOfCoordinates];
        pointLat = new float[numberOfCoordinates];

        int nextEdgeIdx = 0;
        int nextCoordIdx = 0;

        for (CoastlineWay currCoastlineWay : importedCoastlines) {

            List<Point> currPoints = currCoastlineWay.getPoints();

            if (currPoints.size() <= 1) {
                continue;
            }

            int currPointsSize = currPoints.size();

            for (int pointIdx = 0; pointIdx < currPointsSize; pointIdx++) {
                Point currPoint = currPoints.get(pointIdx);

                if (pointIdx == currPointsSize - 1) {

                    pointLon[nextCoordIdx] = currPoint.getLon();
                    pointLat[nextCoordIdx] = currPoint.getLat();

                    nextCoordIdx++;

                } else {

                    pointLon[nextCoordIdx] = currPoint.getLon();
                    pointLat[nextCoordIdx] = currPoint.getLat();

                    edgePosStart[nextEdgeIdx] = nextCoordIdx;

                    nextCoordIdx++;
                    nextEdgeIdx++;
                }

            }
        }
    }

    public static float getStartLatByEdgeIdx(int edgeIdx) {
        return pointLat[edgePosStart[edgeIdx]];
    }

    public static float getDestLatByEdgeIdx(int edgeIdx) {
        return pointLat[edgePosStart[edgeIdx]+1];
    }

    public static float getStartLonByEdgeIdx(int edgeIdx) {
        return pointLon[edgePosStart[edgeIdx]];
    }

    public static float getDestLonByEdgeIdx(int edgeIdx) {
        return pointLon[edgePosStart[edgeIdx]+1];
    }

    public static int getNumberOfEdges() {
        return edgePosStart.length;
    }

}
