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
        System.out.println("ttt: grid init");
        DynamicGrid.currentEdgeIds = edges;
        DynamicGrid.currentEdgeCount = edgeCount;
        DynamicGrid.allEdgeIds = Arrays.stream(edges).map(int[]::clone).toArray(int[][]::new);
        DynamicGrid.allEdgeCount = Arrays.copyOf(edgeCount, edgeCount.length);
    }

    public static void testStartEdges() {
        for (int i = Nodes.getSize() - 1; i >= 0 ; i--) {
            for (int j = 0; j < currentEdgeCount[i]; j++) {
                int edgeId = currentEdgeIds[i][j];
                int otherEdgeLen = getBackwardsEdgeDist(i, Edges.getDest(edgeId));
                if(Edges.getDist(edgeId) != otherEdgeLen) {
                    System.out.println("ttt");
                    System.exit(-100);
                }
            }
        }
    }

    private static int getBackwardsEdgeDist(int start, int dest) {
        for (int i = 0; i < currentEdgeCount[dest]; i++) {
            if(Edges.getDest(currentEdgeIds[dest][i]) == start) {
                return Edges.getDist(currentEdgeIds[dest][i]);
            }
        }
        return -1;
    }

    public static int[] getCurrentEdges(int nodeId) {
        return currentEdgeIds[nodeId];
    }

    public static int getCurrentEdgeCount(int nodeId) {
        return currentEdgeCount[nodeId];
    }

    public static int[] getAllEdges(int nodeId) {
        return allEdgeIds[nodeId];
    }

    public static int getAllEdgeCount(int nodeId) {
        return allEdgeCount[nodeId];
    }

    public static void addEdge(int nodeId, int edgeId) {
        if(currentEdgeIds[nodeId].length == currentEdgeCount[nodeId]) {
            growCurrent(nodeId);
        }
        if(allEdgeIds[nodeId].length == allEdgeCount[nodeId]) {
            growAll(nodeId);
        }

        currentEdgeIds[nodeId][currentEdgeCount[nodeId]] = edgeId;
        currentEdgeCount[nodeId]++;
        allEdgeIds[nodeId][allEdgeCount[nodeId]] = edgeId;
        allEdgeCount[nodeId]++;
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

    public static void removeBackwardsEdge(int startId, int destId) {
        int position = -1; // -1 so it crashes if an unexpected error occurs ttt
        for (int i = 0; i < currentEdgeCount[destId]; i++) {
            if(Edges.getDest(currentEdgeIds[destId][i]) == startId) {
                position = i;
                break;
            }
        }
        if(position == -1) {
            System.out.println("a 11");
        }
        currentEdgeIds[destId][position] = currentEdgeIds[destId][currentEdgeCount[destId] - 1];
        currentEdgeCount[destId]--;
    }

    public static void removeNode(int nodeId) {
        for (int i = 0; i < currentEdgeCount[nodeId]; i++) {
            removeBackwardsEdge(nodeId, Edges.getDest(currentEdgeIds[nodeId][i]));
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
                        noEdges++;
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
            Nodes.initializeLvls(noNodes);


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

    public static int[][] getCurrentEdgeIds() {
        return currentEdgeIds;
    }

    public static int[] getCurrentEdgeCount() {
        return currentEdgeCount;
    }

    public static int[][] getAllEdgeIds() {
        return allEdgeIds;
    }

    public static int[] getAllEdgeCount() {
        return allEdgeCount;
    }

    public static void setCurrentEdgeIds(int[][] currentEdgeIds) {
        DynamicGrid.currentEdgeIds = currentEdgeIds;
    }

    public static void setCurrentEdgeCount(int[] currentEdgeCount) {
        DynamicGrid.currentEdgeCount = currentEdgeCount;
    }

    public static void setAllEdgeIds(int[][] allEdgeIds) {
        DynamicGrid.allEdgeIds = allEdgeIds;
    }

    public static void setAllEdgeCount(int[] allEdgeCount) {
        DynamicGrid.allEdgeCount = allEdgeCount;
    }
}
