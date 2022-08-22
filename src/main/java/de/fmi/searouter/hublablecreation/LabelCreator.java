package de.fmi.searouter.hublablecreation;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LabelCreator {
    private static final String FMI_FILE_NAME = "exported_grid.fmi";

    private static void contractSingleNode(int nodeId) {
        //get all relevant edge ids
        int edgeCount = DynamicGrid.getCurrentEdgeCount(nodeId);
        int[] edges = DynamicGrid.getCurrentEdges(nodeId);

        //get all neighbor node ids
        int[] neighbors = new int[edgeCount];
        for (int i = 0; i < edgeCount; i++) {
            neighbors[i] = Edges.getDest(edges[i]);
        }
    }

    private static void calculateCH() {
        // first, get a random order in which the nodes are contracted
        int  originalNodeCount = Nodes.getSize();
        int[] calcOrder = new int[originalNodeCount];
        for (int i = 0; i < originalNodeCount; i++) {
            calcOrder[i] = i;
        }

        //shuffle somewhat randomly, no perfection required
        Random rnd = ThreadLocalRandom.current();
        for (int i = 0; i < originalNodeCount; i++)
        {
            int index = rnd.nextInt(originalNodeCount - 1);
            // Simple swap
            int tmp = calcOrder[index];
            calcOrder[index] = calcOrder[i];
            calcOrder[i] = tmp;
        }

        //for every node, calculate contraction
    }

    public static void main(String[] args) {
        // Start time of the whole pre-processing for tracking time statistics
        Date startTime = new Date();

        try {
            DynamicGrid.importFmiFile(FMI_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        calculateCH();

        // Calculate the needed time for the pre-processing for time statistics in minutes
        Date endTime = new Date();
        long timeDiffMin = ((endTime.getTime() - startTime.getTime()) / 1000) / 60;
        long timeDiffSec = ((endTime.getTime() - startTime.getTime()) / 1000) % 60;
        System.out.println("Preprocessing finished. Runtime: " + timeDiffMin + ":" + timeDiffSec);
    }
}
