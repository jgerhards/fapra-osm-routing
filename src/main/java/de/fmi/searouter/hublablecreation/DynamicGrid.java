package de.fmi.searouter.hublablecreation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DynamicGrid {
    private static int[][] currentEdgeIds;
    private static int[] currentEdgeCount;
    private static int[][] allEdgeIds;
    private static int[] allEdgeCount;

    public static void initializeEdges(int[][] edges, int[]edgeCount) {
        DynamicGrid.currentEdgeIds = edges;
        DynamicGrid.currentEdgeCount = edgeCount;
        DynamicGrid.allEdgeIds = Arrays.stream(edges).map(int[]::clone).toArray(int[][]::new);
        DynamicGrid.allEdgeCount = Arrays.copyOf(edgeCount, edgeCount.length);
    }

    public static int[] getCurrentEdges(int nodeId) {
        return currentEdgeIds[nodeId];
    }

    public static int getCurrentEdgeCount(int nodeId) {
        return currentEdgeCount[nodeId];
    }

    public static void addEdge(int nodeA, int edgeIdA, int nodeB, int edgeIdB) {
        if(currentEdgeIds[nodeA].length == currentEdgeCount[nodeA]) {
            growCurrent(nodeA);
        }
        if(currentEdgeIds[nodeB].length == currentEdgeCount[nodeB]) {
            growCurrent(nodeB);
        }
        if(allEdgeIds[nodeA].length == allEdgeCount[nodeA]) {
            growAll(nodeA);
        }
        if(allEdgeIds[nodeB].length == allEdgeCount[nodeB]) {
            growAll(nodeB);
        }

        currentEdgeIds[nodeA][currentEdgeCount[nodeA]] = edgeIdA;
        currentEdgeCount[nodeA]++;
        allEdgeIds[nodeA][allEdgeCount[nodeA]] = edgeIdA;
        allEdgeCount[nodeA]++;

        currentEdgeIds[nodeB][currentEdgeCount[nodeB]] = edgeIdB;
        currentEdgeCount[nodeB]++;
        allEdgeIds[nodeB][allEdgeCount[nodeB]] = edgeIdB;
        allEdgeCount[nodeB]++;
    }

    public static int getEdgeId(int startNode, int destNode) {
        int position = -1; // -1 so it crashes if an unexpected error occurs ttt
        for (int i = 0; i < currentEdgeCount[startNode]; i++) {
            if(Edges.getDest(currentEdgeIds[startNode][i]) == destNode) {
                position = i;
                break;
            }
        }
        return currentEdgeIds[startNode][position];
    }

    public static void removeEdge(int nodeA, int nodeB, int edgeIdA) {
        int positionA = -1; // -1 so it crashes if an unexpected error occurs ttt
        for (int i = 0; i < currentEdgeCount[nodeA]; i++) {
            if(currentEdgeIds[nodeA][i] == edgeIdA) {
                positionA = i;
                break;
            }
        }
        currentEdgeIds[nodeA][positionA] = currentEdgeIds[nodeA][currentEdgeCount[nodeA] - 1];
        currentEdgeCount[nodeA]--;

        int positionB = -1; // -1 so it crashes if an unexpected error occurs ttt
        for (int i = 0; i < currentEdgeCount[nodeB]; i++) {
            if(Edges.getDest(currentEdgeIds[nodeB][i]) == edgeIdA) {
                positionB = i;
                break;
            }
        }
        currentEdgeIds[nodeB][positionB] = currentEdgeIds[nodeB][currentEdgeCount[nodeB] - 1];
        currentEdgeCount[nodeB]--;
    }

    public static void removeNode(int nodeId) {
        for (int i = 0; i < currentEdgeCount[nodeId]; i++) {
            removeEdge(nodeId, Edges.getDest(currentEdgeIds[nodeId][i]), currentEdgeIds[nodeId][i]);
        }
        currentEdgeCount[nodeId] = 0;
        currentEdgeIds[nodeId] = null;
    }

    private static void growCurrent(int nodeId) {
        currentEdgeIds[nodeId] = Arrays.copyOf(currentEdgeIds[nodeId], currentEdgeIds[nodeId].length * 2);
    }

    private static void growAll(int nodeId) {
        allEdgeIds[nodeId] = Arrays.copyOf(allEdgeIds[nodeId], allEdgeIds[nodeId].length * 2);
    }


    /**
     * Only temporary used during the import of fmi files needed (for sorting).
     */
    private static class TmpEdge {
        public int startNode;
        public int destNode;
        public int dist;

        public TmpEdge(int startNode, int destNode, int dist) {
            this.startNode = startNode;
            this.destNode = destNode;
            this.dist = dist;
        }
    }

    /**
     * Only temporary during the import of fmi files needed (for sorting).
     */
    private static class TmpNode {
        public int id;
        public double latitude;
        public double longitude;

        public TmpNode(int id, double latitude, double longitude) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Imports a grid graph of a .fmi file format.
     *
     * @param filePath The path within the resources folder where the file to import is placed.
     * @throws IOException If I/O fails.
     */
    public static void importFmiFile(String filePath) throws IOException {
        Resource fmiResource = new ClassPathResource(filePath);
        InputStream inputStream = fmiResource.getInputStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            br.readLine();
            br.readLine();

            // Map file id to internal used node id
            Map<Integer, Integer> fileNodeIdToInternalUsedId = new HashMap<>();

            int noNodes = Integer.parseInt(br.readLine().trim());
            int noEdges = Integer.parseInt(br.readLine().trim());

            List<DynamicGrid.TmpNode> nodeList = new ArrayList<>();

            // Node handling
            for (int nodeIdx = 0; nodeIdx < noNodes; nodeIdx++) {
                String line = br.readLine();
                line = line.trim();

                String[] split = line.split(" ");

                DynamicGrid.TmpNode node = new DynamicGrid.TmpNode(Integer.parseInt(split[0]),
                        Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                nodeList.add(node);

                fileNodeIdToInternalUsedId.put(node.id, nodeIdx);
            }

            // Sort node list by id
            nodeList.sort(new Comparator<DynamicGrid.TmpNode>() {
                @Override
                public int compare(DynamicGrid.TmpNode o1, DynamicGrid.TmpNode o2) {
                    return Integer.compare(fileNodeIdToInternalUsedId.get(o1.id), fileNodeIdToInternalUsedId.get(o2.id));
                }
            });

            // Edge handling
            List<DynamicGrid.TmpEdge> edgeList = new ArrayList<>();

            for (int edgeIdx = 0; edgeIdx < noEdges; edgeIdx++) {
                String line = br.readLine();
                line = line.trim();

                String[] split = line.split(" ");
                DynamicGrid.TmpEdge edge = new DynamicGrid.TmpEdge(
                        fileNodeIdToInternalUsedId.get(Integer.parseInt(split[0])),
                        fileNodeIdToInternalUsedId.get(Integer.parseInt(split[1])),
                        Integer.parseInt(split[2])
                );


                edgeList.add(edge);
            }

            // Sort edges by start ids
            edgeList.sort(new Comparator<DynamicGrid.TmpEdge>() {
                @Override
                public int compare(DynamicGrid.TmpEdge o1, DynamicGrid.TmpEdge o2) {
                    return Integer.compare(o1.startNode, o2.startNode);
                }
            });

            // Build an adjacency map for the following operations
            Map<Integer, List<DynamicGrid.TmpEdge>> adjacenceMap = new HashMap<>();
            for (int edgeIdx = 0; edgeIdx < noEdges; edgeIdx++) {
                int currStartNodeID = edgeList.get(edgeIdx).startNode;
                if (!adjacenceMap.containsKey(currStartNodeID)) {
                    adjacenceMap.put(currStartNodeID, new ArrayList<>());
                }

                adjacenceMap.get(currStartNodeID).add(edgeList.get(edgeIdx));
            }

            // Assure that the graph is undirected by adding missing unidirectional edges
            List<DynamicGrid.TmpEdge> additionalEdgesThatWereMissing = new ArrayList<>();
            for (Map.Entry<Integer, List<DynamicGrid.TmpEdge>> e : adjacenceMap.entrySet()) {

                List<DynamicGrid.TmpEdge> reverseEdgeStartNodesToCheck = e.getValue();
                boolean oppositeEdgeFound = false;

                for (DynamicGrid.TmpEdge revEdge : reverseEdgeStartNodesToCheck) {
                    List<DynamicGrid.TmpEdge> toCheck = adjacenceMap.get(revEdge.destNode);
                    for (DynamicGrid.TmpEdge edges : toCheck) {
                        if (edges.startNode == revEdge.destNode && edges.destNode == revEdge.startNode) {
                            oppositeEdgeFound = true;
                            break;
                        }
                    }
                    if (!oppositeEdgeFound) {
                        additionalEdgesThatWereMissing.add(new DynamicGrid.TmpEdge(revEdge.destNode, revEdge.startNode, revEdge.dist));
                        System.out.println("Added edge " + revEdge.destNode + " | " + revEdge.startNode);
                    }
                }

            }
            edgeList.addAll(additionalEdgesThatWereMissing);

            // Sort edges by start ids
            edgeList.sort(new Comparator<DynamicGrid.TmpEdge>() {
                @Override
                public int compare(DynamicGrid.TmpEdge o1, DynamicGrid.TmpEdge o2) {
                    return Integer.compare(o1.startNode, o2.startNode);
                }
            });

            // Fill arrays
            double[] latitude = new double[noNodes];
            double[] longitude = new double[noNodes];

            for (int i = 0; i < latitude.length; i++) {
                latitude[i] = nodeList.get(i).latitude;
                longitude[i] = nodeList.get(i).longitude;
            }

            Nodes.setLatitude(latitude);
            Nodes.setLongitude(longitude);


            int[] startNode = new int[noEdges];
            int[] destNode = new int[noEdges];
            int[] dist = new int[noEdges];

            for (int i = 0; i < startNode.length; i++) {
                startNode[i] = edgeList.get(i).startNode;
                destNode[i] = edgeList.get(i).destNode;
                dist[i] = edgeList.get(i).dist;
            }

            Edges.setOriginalEdgeStart(startNode);
            Edges.setOriginalEdgeDest(destNode);
            Edges.setOriginalEdgeDist(dist);
            Edges.initializeForShortcutEdges(noEdges);

            // sort edge ids based on start node ids
            int[][] sortedEdges = new int[noNodes][4]; //at most 4 edges are connected
            int[] edgeCounts = new int[noNodes];
            Arrays.fill(edgeCounts, 0);
            for (int i = 0; i < noEdges; i++) {
                sortedEdges[startNode[i]][edgeCounts[startNode[i]]] = i;
                edgeCounts[startNode[i]]++;
            }

            initializeEdges(sortedEdges, edgeCounts);

            br.close();

        }
    }
}
