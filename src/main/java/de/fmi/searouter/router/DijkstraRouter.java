package de.fmi.searouter.router;

import de.fmi.searouter.grid.Edge;
import de.fmi.searouter.grid.Grid;
import de.fmi.searouter.grid.Node;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DijkstraRouter implements Router {

    private int[] currDistanceToNode;
    private int[] previousNode;
    private PriorityQueue<HeapElement> vertexHeapQ;
    private List<Integer> nodeTouched;

    private class HeapElement implements Comparable {
        public int dist;
        public int node;
        public int previousNode;

        @Override
        public int compareTo(Object o) {
            HeapElement toCompare = (HeapElement) o;
            return Integer.compare(dist, toCompare.dist);
        }

        public HeapElement(int dist, int node, int previousNode) {
            this.dist = dist;
            this.node = node;
            this.previousNode = previousNode;
        }
    }

    public DijkstraRouter() {
        this.currDistanceToNode = new int[Node.getSize()];
        this.previousNode = new int[Node.getSize()];
        this.vertexHeapQ = new PriorityQueue<HeapElement>();
        this.nodeTouched = new ArrayList<>();

        for (int nodeIdx = 0; nodeIdx < Node.getSize(); nodeIdx++) {
            currDistanceToNode[nodeIdx] = Integer.MAX_VALUE;
            previousNode[nodeIdx] = -1;
        }

    }

    private void resetState() {
        for (Integer nodeId : nodeTouched) {
            currDistanceToNode[nodeId] = Integer.MAX_VALUE;
            previousNode[nodeId] = -1;
        }
        vertexHeapQ.clear();
        nodeTouched.clear();
    }

    @Override
    public RoutingResult route(int startNodeIdx, int destNodeIdx) {
        long startTime = System.nanoTime();
        resetState();

        vertexHeapQ.add(new HeapElement(0, startNodeIdx, startNodeIdx));

        while (!vertexHeapQ.isEmpty()) {

            HeapElement nodeToHandle = vertexHeapQ.poll();

            if (nodeToHandle.dist >= currDistanceToNode[nodeToHandle.node]) {
                continue;
            }

            currDistanceToNode[nodeToHandle.node] = nodeToHandle.dist;
            previousNode[nodeToHandle.node] = nodeToHandle.previousNode;
            nodeTouched.add(nodeToHandle.node);

            // Break early if target node reached
            if (nodeToHandle.node == destNodeIdx) {
                break;
            }

            for (int neighbourEdgeId = Grid.offset[nodeToHandle.node]; neighbourEdgeId < Grid.offset[nodeToHandle.node + 1]; ++neighbourEdgeId) {

                int destinationVertexId = Edge.getDest(neighbourEdgeId);

                // If the edge destination vertex is not in the vertexSetQ
                //if (!vertexSetQ.contains(destinationVertexId)) {
                //    continue;
                //}

                // Calculate the distance to the destination vertex using the current edge
                int newDistanceOverThisEdgeToDestVertex = currDistanceToNode[nodeToHandle.node] + Edge.getDist(neighbourEdgeId);

                // If the new calculated distance to the destination vertex is lower as the previously known, update the corresponding data stucutres
                if (newDistanceOverThisEdgeToDestVertex < currDistanceToNode[destinationVertexId]) {
                        vertexHeapQ.add(new HeapElement(newDistanceOverThisEdgeToDestVertex, destinationVertexId, nodeToHandle.node));
                }

            }
        }

        List<Integer> path = new ArrayList<>();
        int currNodeUnderInvestigation = destNodeIdx;

        path.add(destNodeIdx);
        while (currNodeUnderInvestigation != startNodeIdx) {
            int previousNodeIdx = previousNode[currNodeUnderInvestigation];
            path.add(previousNode[currNodeUnderInvestigation]);
            currNodeUnderInvestigation = previousNodeIdx;
        }

        // Reverse order of path and save it to array
        Collections.reverse(path);
        long stopTime = System.nanoTime();

        return new RoutingResult(path, currDistanceToNode[destNodeIdx], (double) (stopTime - startTime) / 1000000);
    }

    public int getVertexWithMinimalDistance(Set<Integer> vertexSet, int[] currentDistancesToNode) {

        int minDistance = Integer.MAX_VALUE;
        int minDistanceNodeIdx = -1;

        for (Integer nodeIdx : vertexSet) {
            int nextDistanceToCompare = currentDistancesToNode[nodeIdx];
            if (nextDistanceToCompare < minDistance) {
                minDistance = currentDistancesToNode[nodeIdx];
                minDistanceNodeIdx = nodeIdx;
            }
        }

        return minDistanceNodeIdx;
    }
}
