package de.fmi.searouter.hublabeldata;

import de.fmi.searouter.hublablecreation.CHData;

import java.io.*;

public class HubLStore implements Serializable {
    private int hlLevel;
    private double[] longitudes;
    private double[] latitudes;
    private int[] levels;
    private int[] edgesOffset;
    private int[] edges;
    private int[] labelOffset;
    private int[] labelNode;
    private int[] labelEdge;
    private int[] labelDist;

    private int[] dest;
    private int[] dist;
    private int firstShortcutIdx;
    private int[] shortcutParts;

    public static boolean readData(String filename) {
        File serializationFile = new File(filename);
        if(!serializationFile.exists()) {
            return false;
        }

        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            HubLStore data = (HubLStore) in.readObject();

            in.close();
            file.close();

            data.setData();
            System.out.println("hub label data has been deserialized");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return true;
    }

    public static void storeData(String filename) {
        HubLStore data = new HubLStore();
        // Serialization
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(filename);
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

    private HubLStore() {
        hlLevel = HubLNodes.getHlLevel();
        longitudes = HubLNodes.getLongitudes();
        latitudes = HubLNodes.getLatitudes();
        levels = HubLNodes.getLevels();
        edgesOffset = HubLNodes.getEdgesOffset();
        edges = HubLNodes.getEdges();
        labelOffset = HubLNodes.getLabelOffset();
        labelNode = HubLNodes.getLabelNode();
        labelEdge = HubLNodes.getLabelEdge();
        labelDist = HubLNodes.getLabelDist();

        dest = HubLEdges.getDest();
        dist = HubLEdges.getDist();
        firstShortcutIdx = HubLEdges.getFirstShortcutIdx();
        shortcutParts = HubLEdges.getShortcutParts();
    }

    private void setData() {
        HubLNodes.setHlLevel(hlLevel);
        HubLNodes.setLongitudes(longitudes);
        HubLNodes.setLatitudes(latitudes);
        HubLNodes.setLevels(levels);
        HubLNodes.setEdgesOffset(edgesOffset);
        HubLNodes.setEdges(edges);
        HubLNodes.setLabelOffset(labelOffset);
        HubLNodes.setLabelNode(labelNode);
        HubLNodes.setLabelEdge(labelEdge);
        HubLNodes.setLabelDist(labelDist);

        HubLEdges.setDest(dest);
        HubLEdges.setDist(dist);
        HubLEdges.setFirstShortcutIdx(firstShortcutIdx);
        HubLEdges.setShortcutParts(shortcutParts);
    }
}
