package de.fmi.searouter.hublablecreation;

import de.fmi.searouter.hublabeldata.HubLEdges;
import de.fmi.searouter.hublabeldata.HubLNodes;
import de.fmi.searouter.hublabeldata.HubLStore;
import de.fmi.searouter.utils.IntArrayList;
import de.fmi.searouter.utils.IntArraySet;
import de.fmi.searouter.utils.OrderedIntSet;

import java.io.*;
import java.util.*;

/**
 * Class used to create hub labels based on grid graph data. This also includes all other data required by
 * the hub label routing algorithm.
 */
public class LabelCreator {
    private static final String FMI_FILE_NAME = "oceanfmi.sec";
    //private static final String FMI_FILE_NAME = "exported_grid.fmi";
    private static final String HUB_LABEL_FILE_NAME = "hub_label_data";
    private static final int NUM_OF_THREADS = 32;
    private static final int NUM_OF_NO_LABEL_LVLS = 1;

    //used to keep track of which nodes were contracted using contraction hierarchies
    private static boolean[] contracted;
    //used to check if a node is a neighbour of one that is to be contracted in a given iteration
    private static boolean[] isNeighbour;
    //tracks which shortcuts already exist to prevent inconsistent shortcuts via different intermediate nodes
    private static IntArraySet[] alreadyShortcut;
    //the order in which nodes are processed
    private static int[] calcOrder;
    //threads used to contract nodes
    private static List<CHDijkstra> threads;
    //nodes to process in a given iteration
    private static final IntArrayList nodesToCalc = new IntArrayList(50000);
    //number of nodes not yet contracted
    private static int nonContractedNum;
    //total number of nodes
    private static int nodeCount;

    /**
     * calculate the next level of contractions.
     * @param lvl the current level
     */
    private static void calcNextLvl(int lvl) {
        nodesToCalc.clear();
        Arrays.fill(isNeighbour, false); //important: do not reset which nodes are already contracted
        for (int i = 0; i < nodeCount; i++) {
            int nodeId = calcOrder[i];
            if(!contracted[nodeId] && !isNeighbour[nodeId]) {
                //contract this node in this iteration
                contracted[nodeId] = true;
                isNeighbour[nodeId] = true;
                nonContractedNum--;
                int edgeCount = DynamicGrid.getCurrentEdgeCount(nodeId);
                int[] edges = DynamicGrid.getCurrentEdges(nodeId);
                for (int j = 0; j < edgeCount; j++) {
                    //mark neighbours so no two directly connected nodes are on the same level
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
        for (int i = 0; i < calcNum; i++) { //distribute nodes to threads
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
            //add shortcuts to grid
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
            //remove contracted nodes
            DynamicGrid.removeNode(nodesToCalc.get(i));
        }
    }

    /**
     * Calculate the contraction hierarchy.
     */
    private static void calculateCH() {
        threads = new ArrayList<>();
        // first, get a random order in which the nodes are contracted
        nodeCount = Nodes.getNodeCount();
        alreadyShortcut = new IntArraySet[nodeCount];
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
            System.out.println("contracting level: " + lvl + ", non contracted nodes: " + nonContractedNum);
        }
    }

    /**
     * Calculate labels based on data obtained from contraction hierarchies.
     */
    private static void calcLabels() {
        OrderedIntSet changeIndices = createHLCalcOrder();
        int indicesCount = changeIndices.size();
        List<Thread> threadList = new ArrayList<>();

        int maxCalcIdx = indicesCount - (NUM_OF_NO_LABEL_LVLS + 1);
        for (int i = 0; i < maxCalcIdx; i++) {  //note: not all levels receive labels in order to save memory
            System.out.println("Label calculation processing iteration " + i + " time: " + new Date());
            threadList.clear();
            int startIdx = changeIndices.get(i);
            int endIdx = changeIndices.get(i + 1);
            int minThreadNum = endIdx - startIdx;
            System.out.println("Number of nodes in this iteration: " + minThreadNum);

            if(minThreadNum > NUM_OF_THREADS) {
                int numOfNodes = minThreadNum / NUM_OF_THREADS;
                int threadStartIdx = startIdx;
                for (int j = 1; j < NUM_OF_THREADS; j++) {
                    int threadEndIdx = threadStartIdx + numOfNodes;
                    threadList.add(new HLDijkstra(threadStartIdx, threadEndIdx, calcOrder));
                    threadStartIdx = threadEndIdx;
                }
                threadList.add(new HLDijkstra(threadStartIdx, endIdx, calcOrder));
            } else {
                for (int j = startIdx; j < endIdx; j++) {
                    threadList.add(new HLDijkstra(j, j + 1, calcOrder));
                }
            }

            for(Thread thread : threadList) {
                thread.start();
            }
            for(Thread thread : threadList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * Create calculation order for hub label processing. This is based on the levels each node was
     * assigned during calculation of the contraction hierarchy.
     * @return An OrderedIntSet containing indices at which the next level of nodes is reached
     */
    private static OrderedIntSet createHLCalcOrder() {
        calcOrder = new int[Nodes.getNodeCount()];
        int n = calcOrder.length;
        for (int i = 0; i < n; i++) {
            calcOrder[i] = i;
        }

        //arrange node ids in the correct order based on level
        int temp;
        for(int i=0; i < n; i++){
            for(int j=1; j < (n-i); j++){
                if(Nodes.getNodeLvl(calcOrder[j-1]) < Nodes.getNodeLvl(calcOrder[j])){
                    //swap elements
                    temp = calcOrder[j-1];
                    calcOrder[j-1] = calcOrder[j];
                    calcOrder[j] = temp;
                }
            }
        }

        //find indices at which the nodes of the next level begin
        OrderedIntSet changeIndices = new OrderedIntSet(false, 650, 10);
        int prevLvl = 0;
        changeIndices.insertTail(0);
        for (int i = 0; i < n; i++) {
            int currLvl = Nodes.getNodeLvl(calcOrder[i]);
            if(currLvl < prevLvl) {
                changeIndices.insertTail(i);
            }
            prevLvl = currLvl;
        }
        changeIndices.insertTail(n);
        return changeIndices;
    }

    /**
     * Store data relevant for the routing algorithm after preprocessing.
     */
    private static void serializeHubLData() {
        HubLEdges.initialize();
        HubLNodes.initHlLvl(NUM_OF_NO_LABEL_LVLS);
        HubLNodes.initNodeData();
        HubLNodes.initEdgeInfo();
        try {
            HubLNodes.initLabelInfo();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        HubLStore.storeData(HUB_LABEL_FILE_NAME);
    }

    /**
     * Create Labels from original grid data (in an FMI file format). During this processing,
     * intermediate data will be stored at certain points. This data can then be used to later on
     * skip certain preprocessing steps.
     * @param args
     */
    public static void main(String[] args) {
        // Start time of the whole pre-processing for tracking time statistics
        Date startTime = new Date();

        if(!CHData.readData()) { //check if Ch data is already present
            try {
                DynamicGrid.importFmiFile(FMI_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            calculateCH();
            CHData.storeData();
        }

        System.out.println("step 1 complete");

        if(!TmpLabelData.readData()) { //check if temporary label data is already present
            Labels.initialize(Nodes.getNodeCount());
            System.out.println("step 2.1 complete");
            calcLabels();
            System.out.println("step 2.2 complete");
            TmpLabelData.storeData();
        }

        System.out.println("step 2 complete");

        //bring data into correct format and store it persistently
        serializeHubLData();

        // Calculate the needed time for the pre-processing for time statistics in minutes
        Date endTime = new Date();
        long timeDiffMin = ((endTime.getTime() - startTime.getTime()) / 1000) / 60;
        long timeDiffSec = ((endTime.getTime() - startTime.getTime()) / 1000) % 60;
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("Preprocessing finished. Runtime: " + timeDiffMin + ":" + timeDiffSec);

        //also, include heapsize to allow better estimates of memory usage
        System.out.println("Heapsize: " + heapSize);
    }
}
