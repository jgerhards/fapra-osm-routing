package de.fmi.searouter.hublablecreation;

import java.io.*;

/**
 * This class is used to store data used by the preprocessing algorithm. Specifically, it contains data that will be
 * transformed into a different format and stored for the routing algorithm in the next step. Apart from that, this
 * class serves no further purpose.
 */
public class TmpLabelData implements Serializable{
    public static final String SERIALIZATION_FILE_NAME = "tmp_label_data.ser";

    //data of labels, stored in Labels class
    private int[][] labelNodes;
    private int[][] labelEdges;
    private int[][] labelDist;

    /**
     * Store data from data structures in {@link Labels} in a file for later use.
     */
    public static void storeData() {
        TmpLabelData data = new TmpLabelData();
        data.labelNodes = Labels.getLabelNodes();
        data.labelEdges = Labels.getLabelEdges();
        data.labelDist = Labels.getLabelDist();

        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(SERIALIZATION_FILE_NAME);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(data);

            out.close();
            file.close();

            System.out.println("tmp label data has been serialized");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Read data from a file and insert it into data structures in {@link Labels}.
     * @return true if it was successful, false if no such file exists
     */
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
            TmpLabelData data = (TmpLabelData) in.readObject();

            in.close();
            file.close();

            //insert data into appropriate structures
            Labels.setLabelNodes(data.labelNodes);
            Labels.setLabelEdges(data.labelEdges);
            Labels.setLabelDist(data.labelDist);
            System.out.println("tmp label data has been deserialized");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return true;
    }

}
