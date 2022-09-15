package de.fmi.searouter.hublabeldata;

import java.io.*;

/**
 * This class is used to store data used by the routing algorithm. Apart from that, it serves no further purpose.
 */
public class HubLStore implements Serializable {
    //fields from HubLNodes
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

    //fields from HubLEdges
    private int[] dest;
    private int[] dist;
    private int firstShortcutIdx;
    private int[] shortcutParts;

    /**
     * Read data from a file with a given path and insert it into data structures in {@link HubLNodes}
     * and {@link HubLEdges}.
     * @param filename the path of the file
     * @return true if it was successful, false if no such file exists
     */
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
            System.out.println("Hub label data has been deserialized");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return true;
    }

    /**
     * Store data from data structures in {@link HubLNodes} and {@link HubLEdges} in a file with a given path.
     * @param filename the path of the file
     */
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

            System.out.println("Hub label data has been serialized");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Constructor. Takes data from internal structures in order to fill the object fields.
     */
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

    /**
     * Insert data into appropriate data structures.
     */
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
