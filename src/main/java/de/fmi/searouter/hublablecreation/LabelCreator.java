package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.utils.IntArrayList;
import de.fmi.searouter.utils.IntArraySet;

import java.io.IOException;
import java.util.*;

public class LabelCreator {
    private static final String FMI_FILE_NAME = "exported_grid.fmi";
    private static final int NUM_OF_THREADS = 32;
    private static boolean[] contracted;
    private static boolean[] isNeighbour;
    private static IntArraySet[] alreadyShortcut;
    private static int nonContractedNum;
    private static int nodeCount;
    private static int[] calcOrder;
    private static final IntArrayList nodesToCalc = new IntArrayList(50000);
    private static List<CHDijkstra> threads;

    private static void calcNextLvl(int lvl) {
        nodesToCalc.clear();
        Arrays.fill(isNeighbour, false);
        for (int i = 0; i < nodeCount; i++) {
            int nodeId = calcOrder[i];
            if(!contracted[nodeId] && !isNeighbour[nodeId]) {
                contracted[nodeId] = true;
                isNeighbour[nodeId] = true;
                nonContractedNum--;
                int edgeCount = DynamicGrid.getCurrentEdgeCount(nodeId);
                int[] edges = DynamicGrid.getCurrentEdges(nodeId);
                for (int j = 0; j < edgeCount; j++) {
                    //System.out.println("ttt: " + nodeId);
                    isNeighbour[Edges.getDest(edges[j])] = true;
                }
                nodesToCalc.add(nodeId);
                Nodes.setNodeLevel(nodeId, lvl);
            }
        }

        int calcNum = nodesToCalc.getLen();
        threads.clear();
        for (int i = 0; i < NUM_OF_THREADS; i++) {
            threads.add(new CHDijkstra());
        }
        for (int i = 0; i < calcNum; i++) {
            threads.get(i % NUM_OF_THREADS).addNodeId(nodesToCalc.get(i));
        }

        for (int i = 0; i < NUM_OF_THREADS; i++) {
            threads.get(i).start();
        }
        for (int i = 0; i < NUM_OF_THREADS; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < NUM_OF_THREADS; i++) {
            CHDijkstra thread = threads.get(i);
            IntArrayList shortcuts = thread.getShortcuts();
            int numOfShortcuts = shortcuts.getLen() / 5;
            for (int j = 0; j < numOfShortcuts; j++) {
                int idx = j * 5;
                int startIdx = shortcuts.get(idx);
                int destIdx = shortcuts.get(idx + 1);
                if(alreadyShortcut[startIdx].contains(destIdx)) {
                    continue;
                }
                int firstEdgeId = Edges.addShortcutEdge(startIdx, destIdx,
                        shortcuts.get(idx + 2), shortcuts.get(idx + 3), shortcuts.get(idx + 4));
                DynamicGrid.addEdge(shortcuts.get(idx), firstEdgeId);
                alreadyShortcut[startIdx].add(destIdx);
            }
        }

        for (int i = nodesToCalc.getLen() - 1; i >= 0; i--) {
            DynamicGrid.removeNode(nodesToCalc.get(i));
        }
    }

    private static void calculateCH() {
        threads = new ArrayList<>();
        // first, get a random order in which the nodes are contracted
        nodeCount = Nodes.getSize();
        alreadyShortcut = new IntArraySet[nodeCount];
        System.out.println("ttt: node count: " + nodeCount);
        calcOrder = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            alreadyShortcut[i] = new IntArraySet(1, 20);
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

        //at the start, no nodes are contracted
        nonContractedNum = nodeCount;
        contracted = new boolean[nodeCount];
        Arrays.fill(contracted, false);
        isNeighbour = new boolean[nodeCount];

        int lvl = 0;
        while(nonContractedNum > 0) {
            calcNextLvl(lvl);
            lvl++;
            System.out.println("ttt: ---------------------------- level: " + lvl +
                    ", non contracted nodes: " + nonContractedNum);
        }
    }

    private static void calcInitialLabels() {
        HLDijkstra[] threads = new HLDijkstra[NUM_OF_THREADS];
        int numOfNodes = Nodes.getSize() / NUM_OF_THREADS;
        int startIdx = 0;
        for (int i = 1; i < NUM_OF_THREADS; i++) { //note: start at 1
            int endIdx = startIdx + numOfNodes;
            threads[i] = new HLDijkstra(startIdx, endIdx);
            startIdx = endIdx;
        }
        threads[0] = new HLDijkstra(startIdx, Nodes.getSize());

        for (int i = 0; i < NUM_OF_THREADS; i++) {
            threads[i].start();
        }
        for (int i = 0; i < NUM_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Start time of the whole pre-processing for tracking time statistics
        Date startTime = new Date();

        if(false) { //todo: used for testing, delete when no longer needed
            //DynamicGrid.testStartEdges();

            // link: https://jlazarsfeld.github.io/ch.150.project/sections/8-contraction/
            //the line directly below can be added in order to force calculation order. insert in calculateCH function
            calcOrder = new int[]{4, 2, 1, 3, 0};
            Nodes.setLatitude(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            Nodes.initializeLvls(5);
            Edges.setNumOfOriginalEdges(5);
            int[] startNode = new int[]{0, 2, 1, 2, 2, 3, 1, 4, 3, 4};
            Edges.setOriginalEdgeStart(startNode);
            Edges.setOriginalEdgeDest(new int[]{2, 0, 2, 1, 3, 2, 4, 1, 4, 3});
            Edges.setOriginalEdgeDist(new int[]{10, 10, 3, 3, 6, 6, 5, 5, 5, 5});
            Edges.initializeForShortcutEdges(10);

            int noNodes = 5;
            int noEdges = 10;
            // sort edge ids based on start node ids
            int[][] sortedEdges = new int[noNodes][4]; //at most 4 edges are connected
            int[] edgeCounts = new int[noNodes];
            Arrays.fill(edgeCounts, 0);
            for (int i = 0; i < noEdges; i++) {
                sortedEdges[startNode[i]][edgeCounts[startNode[i]]] = i;
                edgeCounts[startNode[i]]++;
            }
            DynamicGrid.initializeEdges(sortedEdges, edgeCounts);
            calculateCH();

            System.out.println(Arrays.toString(Edges.getShortcutEdgeStart()));
            System.out.println(Arrays.toString(Edges.getShortcutEdgeDest()));
            System.out.println(Arrays.toString(Edges.getShortcutEdgeDist()));

            Labels.initialize(noNodes);
            HLDijkstra testDijkstra = new HLDijkstra(0, 5);
            testDijkstra.start();
            try {
                testDijkstra.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        if(!CHData.readData()) {
            try {
                DynamicGrid.importFmiFile(FMI_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            calculateCH();
            CHData.storeData();
        }

        if(!TmpLabelData.readData()) {
            Labels.initialize(Nodes.getSize());
            calcInitialLabels();
            TmpLabelData.storeData();
        }

        // Calculate the needed time for the pre-processing for time statistics in minutes
        Date endTime = new Date();
        long timeDiffMin = ((endTime.getTime() - startTime.getTime()) / 1000) / 60;
        long timeDiffSec = ((endTime.getTime() - startTime.getTime()) / 1000) % 60;
        System.out.println("Preprocessing finished. Runtime: " + timeDiffMin + ":" + timeDiffSec);
    }
}
