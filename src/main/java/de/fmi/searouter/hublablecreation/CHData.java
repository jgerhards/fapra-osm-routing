package de.fmi.searouter.hublablecreation;

import java.io.*;

public class CHData implements Serializable {
    public static final String SERIALIZATION_FILE_NAME = "ch_data.ser";

    //edges fields
    private int[] originalEdgeStart;
    private int[] originalEdgeDest;
    private int[] originalEdgeDist;
    private int numOfOriginalEdges;
    private int[] shortcutEdgeStart;
    private int[] shortcutEdgeDest;
    private int[] shortcutEdgeDist;
    private int[] shortcutEdgeParts;
    private int nextShortcutId;
    private int shortcutEdgeSizeIncrease;

    //nodes fields
    private double[] latitude;
    private double[] longitude;
    private int[] levels;

    //dynamic grid fields
    private int[][] currentEdgeIds;
    private int[] currentEdgeCount;
    private int[][] allEdgeIds;
    private int[] allEdgeCount;

    public static boolean readData() {
        File serializationFile = new File(SERIALIZATION_FILE_NAME);
        if(!serializationFile.exists()) {
            return false;
        }

        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(SERIALIZATION_FILE_NAME);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            CHData data = (CHData) in.readObject();

            in.close();
            file.close();

            data.setData();
            System.out.println("CH data has been deserialized");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return true;
    }

    public static void storeData() {
        CHData data = new CHData();
        // Serialization
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(SERIALIZATION_FILE_NAME);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(data);

            out.close();
            file.close();

            System.out.println("CH data has been serialized");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private CHData() {
       originalEdgeStart = Edges.getOriginalEdgeStart();
       originalEdgeDest = Edges.getOriginalEdgeDest();
       originalEdgeDist = Edges.getOriginalEdgeDist();
       numOfOriginalEdges = Edges.getNumOfOriginalEdges();
       shortcutEdgeStart = Edges.getShortcutEdgeStart();
       shortcutEdgeDest = Edges.getShortcutEdgeDest();
       shortcutEdgeDist = Edges.getShortcutEdgeDist();
       shortcutEdgeParts = Edges.getShortcutEdgeParts();
       nextShortcutId = Edges.getNextShortcutId();
       shortcutEdgeSizeIncrease = Edges.getShortcutEdgeSizeIncrease();

       latitude = Nodes.getLatitude();
       longitude = Nodes.getLongitude();
       levels = Nodes.getLevels();

       currentEdgeIds = DynamicGrid.getCurrentEdgeIds();
       currentEdgeCount = DynamicGrid.getCurrentEdgeCount();
       allEdgeIds = DynamicGrid.getAllEdgeIds();
       allEdgeCount = DynamicGrid.getAllEdgeCount();
    }

    private void setData() {
        Edges.setOriginalEdgeStart(originalEdgeStart);
        Edges.setOriginalEdgeDest(originalEdgeDest);
        Edges.setOriginalEdgeDist(originalEdgeDist);
        Edges.setNumOfOriginalEdges(numOfOriginalEdges);
        Edges.setShortcutEdgeStart(shortcutEdgeStart);
        Edges.setShortcutEdgeDest(shortcutEdgeDest);
        Edges.setShortcutEdgeDist(shortcutEdgeDist);
        Edges.setShortcutEdgeParts(shortcutEdgeParts);
        Edges.setNextShortcutId(nextShortcutId);
        Edges.setShortcutEdgeSizeIncrease(shortcutEdgeSizeIncrease);

        Nodes.setLatitude(latitude);
        Nodes.setLongitude(longitude);
        Nodes.setLevels(levels);

        DynamicGrid.setCurrentEdgeIds(currentEdgeIds);
        DynamicGrid.setCurrentEdgeCount(currentEdgeCount);
        DynamicGrid.setAllEdgeIds(allEdgeIds);
        DynamicGrid.setAllEdgeCount(allEdgeCount);
    }
}
