package de.fmi.searouter.grid;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.*;

/**
 * Contains the offset data structure which connects the {@link Edge}s with the {@link Node}s to
 * a graph representation.
 */
public class Grid {

    // Stores for each node id the position in the Edge array where the edges for the respective nodes start.
    public static int[] offset;

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
                    return Integer.compare(o1.id, o2.id);
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

            System.out.println("test");

            br.close();


        }
    }

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

        try {
            importFmiFile("testImport.fmi");

            exportToFmiFile("test2.fmi");
        } catch (IOException e) {
            e.printStackTrace();
        }




    }


}
