package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.OrderedIntSet;

import java.io.*;

public class TmpLabelData implements Serializable{
    public static final String SERIALIZATION_FILE_NAME = "tmp_label_data.ser";

    private int[][] labelNodes;
    private int[][] labelEdges;
    private int[][] labelDist;

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
