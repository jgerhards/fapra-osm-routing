package de.fmi.searouter.hublablecreation;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LabelCreator {
    private static final String FMI_FILE_NAME = "exported_grid.fmi";
    private static CHDijkstra dijkstra;

    private static void contractSingleNode(int nodeId) {
        //get all relevant edge ids
        int edgeCount = DynamicGrid.getCurrentEdgeCount(nodeId);
        int[] edges = DynamicGrid.getCurrentEdges(nodeId);

        //get all neighbor node ids
        int[] neighbors = new int[edgeCount];
        for (int i = 0; i < edgeCount; i++) {
            neighbors[i] = Edges.getDest(edges[i]);
        }

        for (int neighbourId : neighbors) {
            dijkstra.calculateNew(neighbourId, neighbors);
            int numOfShortcuts = dijkstra.findShortcuts(nodeId);
            int[] shortcuts = dijkstra.getShortcuts();
            for (int i = 0; i < numOfShortcuts; i++) {
                int idx = i * 4;
                Edges.addShortcutEdge(neighbourId, shortcuts[idx], shortcuts[idx + 1],
                        shortcuts[idx + 2], shortcuts[idx + 3]);
            }
        }
    }

    private static void calculateCH() {
        LabelCreator.dijkstra = new CHDijkstra();
        // first, get a random order in which the nodes are contracted
        int  originalNodeCount = Nodes.getSize();
        int[] calcOrder = new int[originalNodeCount];
        for (int i = 0; i < originalNodeCount; i++) {
            calcOrder[i] = i;
        }

        //shuffle somewhat randomly, no perfection required
        Random rnd = new Random(123);
        for (int i = 0; i < originalNodeCount; i++) {
            int index = rnd.nextInt(originalNodeCount - 1);
            // Simple swap
            int tmp = calcOrder[index];
            calcOrder[index] = calcOrder[i];
            calcOrder[i] = tmp;
        }

        //set ranks of nodes
        int[] ranks = new int[originalNodeCount];
        for (int i = 0; i < originalNodeCount; i++) {
            ranks[calcOrder[i]] = i;
        }
        Nodes.setRanks(ranks);

        //for every node, calculate contraction
        for (int i = 0; i < originalNodeCount; i++) {
            int nodeID = calcOrder[i];
            contractSingleNode(nodeID);
            DynamicGrid.removeNode(nodeID);
        }
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
