package de.fmi.searouter.grid;

import de.fmi.searouter.utils.IntersectionHelper;
import de.fmi.searouter.router.DijkstraRouter;
import de.fmi.searouter.router.Router;
import de.fmi.searouter.router.RoutingResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Contains the offset data structure which connects the {@link Edge}s with the {@link Node}s to
 * a graph representation. Provides in addition methods for grid operations.
 */
public class Grid {

    // Stores for each node id the position in the Edge array where the edges for the respective nodes start.
    public static int[] offset;

    /**
     *
     * @param latitude (0 to 90°)
     * @param longitude (-180 to 180°)
     * @return The index within the {@link Node} data structure that points to the nearest grid node. -1 if no node exists
     * in the requested plane of integer degrees.
     */
    public static int getNearestGridNodeByCoordinates(double latitude, double longitude) {
        // Strategy: Get all grid nodes on the plane of integer grid numbers and then check manually the distance

        // Integer coordinate degrees to search for
        int iLat = (int) latitude;
        int iLong = (int) longitude;

        List<Integer> candidateNodes = new ArrayList<>();

        // Add all node indices to the candidate node list that are within the integer degree plane
        for (int i = 0; i < Node.getSize(); i++) {
            if (iLat == (int) Node.getLatitude(i) && iLong == (int) Node.getLongitude(i)) {
                candidateNodes.add(i);
            }
        }

        if (candidateNodes.size() <= 0) {
            return -1;
        }

        // For all candidates, calculate the distances to the requested coordinates
        double minDistance = Double.MAX_VALUE;
        int minNodeIdx = 0;
        for (Integer nodeIdx : candidateNodes) {
            double currDistance = IntersectionHelper.getDistance(
                    latitude, longitude,
                    Node.getLatitude(nodeIdx), Node.getLongitude(nodeIdx)
                    );
            if (currDistance < minDistance) {
                minDistance = currDistance;
                minNodeIdx = nodeIdx;
            }
        }

        return minNodeIdx;
    }


    /**
     * Only temporary during the import of fmi files needed (for sorting).
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

            List<TmpNode> nodeList = new ArrayList<>();

            // Node handling
            for (int nodeIdx = 0; nodeIdx < noNodes; nodeIdx++) {
                String line = br.readLine();
                line = line.trim();

                String[] split = line.split(" ");

                TmpNode node = new TmpNode(Integer.parseInt(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                nodeList.add(node);

                fileNodeIdToInternalUsedId.put(node.id, nodeIdx);
            }

            // Sort node list by id
            nodeList.sort(new Comparator<TmpNode>() {
                @Override
                public int compare(TmpNode o1, TmpNode o2) {
                    return Integer.compare(fileNodeIdToInternalUsedId.get(o1.id), fileNodeIdToInternalUsedId.get(o2.id));
                }
            });

            // Edge handling
            List<TmpEdge> edgeList = new ArrayList<>();

            for (int edgeIdx = 0; edgeIdx < noEdges; edgeIdx++) {
                String line = br.readLine();
                line = line.trim();

                String[] split = line.split(" ");
                TmpEdge edge = new TmpEdge(
                        fileNodeIdToInternalUsedId.get(Integer.parseInt(split[0])),
                        fileNodeIdToInternalUsedId.get(Integer.parseInt(split[1])),
                        Integer.parseInt(split[2])
                );


                edgeList.add(edge);
            }

            // Sort edges by start ids
            edgeList.sort(new Comparator<TmpEdge>() {
                @Override
                public int compare(TmpEdge o1, TmpEdge o2) {
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

            Node.setLatitude(latitude);
            Node.setLongitude(longitude);


            int[] startNode = new int[noEdges];
            int[] destNode = new int[noEdges];
            int[] dist = new int[noEdges];

            for (int i = 0; i < startNode.length; i++) {
                startNode[i] = edgeList.get(i).startNode;
                destNode[i] = edgeList.get(i).destNode;
                dist[i] = edgeList.get(i).dist;
            }

            Edge.setStartNode(startNode);
            Edge.setDestNode(destNode);
            Edge.setDist(dist);

            offset = new int[Node.getSize() + 1];

            for (int i = 0; i < Edge.getSize(); ++i) {
                offset[Edge.getStart(i) + 1] += 1;
            }

            for (int i = 1; i < offset.length; ++i) {
                offset[i] += offset[i - 1];
            }

            br.close();


        }
    }

    /**
     * Exports the current grid graph representation (contents of {@link Edge} and {@link Node}).
     *
     * @param filePath The export path (relative to the main directory of this project).
     * @throws IOException If I/O fails.
     */
    public static void exportToFmiFile(String filePath) throws IOException {
        // Get an input stream for the pbf file located in the resources directory

        File f = new File(filePath);
        f.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(f));

        writer.write("#\n\n");

        // Number of nodes
        writer.append(String.valueOf(Node.getSize())).append("\n");
        // Number of edges
        writer.append(String.valueOf(Edge.getSize())).append("\n");

        // Write all nodes
        for (int nodeIdx = 0; nodeIdx < Node.getSize(); nodeIdx++) {
            writer.append(String.valueOf(nodeIdx)).append(" ").append(String.valueOf(Node.getLatitude(nodeIdx))).append(" ").append(String.valueOf(Node.getLongitude(nodeIdx))).append("\n");
        }

        // Write all edges
        for (int edgeIdx = 0; edgeIdx < Edge.getSize(); edgeIdx++) {
            writer.append(String.valueOf(Edge.getStart(edgeIdx))).append(" ").append(String.valueOf(Edge.getDest(edgeIdx))).append(" ").append(String.valueOf(Edge.getDist(edgeIdx))).append("\n");
        }

        writer.close();
    }

    public static void main(String[] args) {

        System.out.println(-180 + (580 % (-180)));

        BigDecimal noA = BigDecimal.valueOf(580);
        System.out.println(BigDecimal.valueOf(-180).add(noA.remainder(BigDecimal.valueOf(180))));

        BigDecimal noB = BigDecimal.valueOf(-500);
        System.out.println(noB.remainder(BigDecimal.valueOf(180)));

        try {
            importFmiFile("testImport.fmi");

            exportToFmiFile("test2.fmi");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int startNode = Grid.getNearestGridNodeByCoordinates(0.0, -0.2);
        System.out.println("ID: " + startNode + " Lat: " + "Node.getLatitude(startNode)" +  " Long: " + "");

        int destNode = Grid.getNearestGridNodeByCoordinates(-0.6, 0.0);
        System.out.println("ID: " + destNode + " Lat: " + Node.getLatitude(destNode) +  " Long: " + Node.getLongitude(destNode));


        Router router = new DijkstraRouter();
        RoutingResult res = router.route(startNode, destNode);
        System.out.println(res);

        RoutingResult rest = router.route(startNode, destNode);
        System.out.println(rest);
    }


}
