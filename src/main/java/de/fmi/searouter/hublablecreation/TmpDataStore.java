package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.OrderedIntSet;

import java.io.*;

public class TmpDataStore implements Serializable{
    public static final String SERIALIZATION_FILE_NAME = "tttt.ser";

    private int[] calcOrder;
    private OrderedIntSet changeIndices;

    /**
     * Reads data from a file and insert it into data structures in {@link Edges}, {@link Nodes} and
     * {@link DynamicGrid}.
     * @return true if it was successful, false if no appropriate file exists
     */
    public static TmpDataStore readData() {
        File serializationFile = new File(SERIALIZATION_FILE_NAME);
        if(!serializationFile.exists()) {
            return null;
        }

        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(SERIALIZATION_FILE_NAME);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            TmpDataStore data = (TmpDataStore) in.readObject();

            in.close();
            file.close();

            System.out.println("order data has been deserialized");
            return data;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    /**
     * Store data from data structures in {@link Edges}, {@link Nodes} and {@link DynamicGrid} in a file.
     */
    public static void storeData(int[] arr, OrderedIntSet set) {
        TmpDataStore data = new TmpDataStore(arr, set);
        // Serialization
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(SERIALIZATION_FILE_NAME);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(data);

            out.close();
            file.close();

            System.out.println("order data has been serialized");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Constructor. Takes data from internal structures in order to fill the object fields.
     */
    private TmpDataStore(int[] arr, OrderedIntSet set) {
        calcOrder = arr;
        changeIndices = set;
    }

    public int[] getCalcOrder() {
        return calcOrder;
    }

    public OrderedIntSet getChangeIndices() {
        return changeIndices;
    }
}
