package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.IntArrayList;

import java.io.IOException;
import java.util.*;

public class LabelCreator {
    private static final String FMI_FILE_NAME = "exported_grid.fmi";
    private static final int THREAD_NUM = 32;
    private static boolean[] contracted;
    private static boolean[] isNeighbour;
    private static int nonContractedNum;
    private static int nodeCount;
    private static IntArrayList nodesToCalc = new IntArrayList(50000);
    private static List<CHDijkstra> threads;

    private static void calcNextLvl(int lvl) {
        nodesToCalc.clear();
        Arrays.fill(isNeighbour, false);
        for (int i = 0; i < nodeCount; i++) {
            if(!contracted[i] && !isNeighbour[i]) {
                contracted[i] = true;
                isNeighbour[i] = true;
                nonContractedNum--;
                int edgeCount = DynamicGrid.getCurrentEdgeCount(i);
                int[] edges = DynamicGrid.getCurrentEdges(i);
                for (int j = 0; j < edgeCount; j++) {
                    isNeighbour[Edges.getDest(edges[i])] = true;
                }
                nodesToCalc.add(i);
                Nodes.setNodeLevel(i, lvl);
            }
        }

        int calcNum = nodesToCalc.getLen();
        for (int i = 0; i < calcNum; i++) {
            threads.get(i % THREAD_NUM).addNodeId(nodesToCalc.get(i));
        }

        for (int i = 0; i < THREAD_NUM; i++) {
            threads.get(i).start();
        }
        for (int i = 0; i < THREAD_NUM; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < THREAD_NUM; i++) {
            CHDijkstra thread = threads.get(i);
            IntArrayList shortcuts = thread.getShortcuts();
            int numOfShortcuts = shortcuts.getLen();
            for (int j = 0; j < numOfShortcuts; j++) {
                int idx = j * 5;
                int firstEdgeId = Edges.addShortcutEdge(shortcuts.get(idx), shortcuts.get(idx + 1),
                        shortcuts.get(idx + 2), shortcuts.get(idx + 3), shortcuts.get(idx + 4));
                DynamicGrid.addEdges(shortcuts.get(idx), firstEdgeId, shortcuts[idx], secondEdgeId); //todo: hier neue edge einfügen und alten knoten löschen?
            }
        }
    }

    private static void calculateCH() {
        threads = new ArrayList<>();
        for (int i = 0; i < THREAD_NUM; i++) {
            threads.add(new CHDijkstra());
        }
        // first, get a random order in which the nodes are contracted
        nodeCount = Nodes.getSize();
        int[] calcOrder = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            calcOrder[i] = i;
        }

        //shuffle somewhat randomly, no perfection required
        Random rnd = new Random(123);
        for (int i = 0; i < nodeCount; i++) {
            int index = rnd.nextInt(nodeCount - 1);
            // Simple swap
            int tmp = calcOrder[index];
            calcOrder[index] = calcOrder[i];
            calcOrder[i] = tmp;
        }

        //at the start, no nodes were contracted
        nonContractedNum = nodeCount;
        contracted = new boolean[nodeCount];
        Arrays.fill(contracted, false);
        isNeighbour = new boolean[nodeCount];

        int multiThreshold = nodeCount - 10000;
        int lvl = 0;
        while(nonContractedNum > multiThreshold) {
            calcNextLvl(lvl);
            lvl++;
        }
        //for every node, calculate contraction
        for (int i = 0; i < nodeCount; i++) {
            if(i == 660554) {
                System.out.println("a6");
            }
            int nodeID = calcOrder[i];
            System.out.println("ttt:-------------------------------------------------------- " + nodeID + ", i: " + i);
            calcNextLvl();
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

        if(!CHData.readData()) {
            calculateCH();
            CHData.storeData();
        }

        // Calculate the needed time for the pre-processing for time statistics in minutes
        Date endTime = new Date();
        long timeDiffMin = ((endTime.getTime() - startTime.getTime()) / 1000) / 60;
        long timeDiffSec = ((endTime.getTime() - startTime.getTime()) / 1000) % 60;
        System.out.println("Preprocessing finished. Runtime: " + timeDiffMin + ":" + timeDiffSec);
    }
}
