package de.fmi.searouter.router;

import de.fmi.searouter.dijkstragrid.Edge;
import de.fmi.searouter.dijkstragrid.Grid;
import de.fmi.searouter.dijkstragrid.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * class used to create a route when given a start and end node.
 */
public class DijkstraRouter implements Router {

    //current distance to the target node
    protected final int[] currDistanceToNode;
    //previous node on the way to the target node
    private final int[] previousNode;
    private final DijkstraHeap vertexHeap;
    private final boolean[] nodeTouched;

    /**
     * constructor. also initializes internal fields
     */
    public DijkstraRouter() {
        this.currDistanceToNode = new int[Node.getSize()];
        this.previousNode = new int[Node.getSize()];
        this.nodeTouched = new boolean[Node.getSize()];
        this.vertexHeap = new DijkstraHeap(this);

        Arrays.fill(currDistanceToNode, Integer.MAX_VALUE);
        Arrays.fill(previousNode, -1);
        Arrays.fill(nodeTouched, false);
    }

    /**
     * resets the state of a previous calculation
     */
    private void resetState() {
        Arrays.fill(currDistanceToNode, Integer.MAX_VALUE);
        Arrays.fill(previousNode, -1);
        Arrays.fill(nodeTouched, false);

        vertexHeap.resetState();
    }

    /**
     * Calculates the shortest path from one start node to a destination node. Node definitions
     * are in {@link Node}, edge definition in {@link Edge} and the relationships between those two
     * data structures in {@link Grid}.
     *
     * @param startNodeIdx The index of the start node (corresponding to {@link Node} indices)
     * @param destNodeIdx The index of the destination node (corresponding to {@link Node} indices)
     * @return a route between start and destination node
     */
    @Override
    public RoutingResult route(int startNodeIdx, int destNodeIdx) {

        long startTime = System.nanoTime();
        resetState();

        currDistanceToNode[startNodeIdx] = 0;
        previousNode[startNodeIdx] = startNodeIdx;
        vertexHeap.add(startNodeIdx);

        boolean routeFound = false;
        while (!vertexHeap.isEmpty()) {
            int nodeToHandleId = vertexHeap.getNext();
            nodeTouched[nodeToHandleId] = true;

            // Break early if target node reached
            if (nodeToHandleId == destNodeIdx) {
                routeFound = true;
                break;
            }

            for (int neighbourEdgeId = Grid.offset[nodeToHandleId]; neighbourEdgeId < Grid.offset[nodeToHandleId + 1]; ++neighbourEdgeId) {

                int destinationVertexId = Edge.getDest(neighbourEdgeId);

                if(nodeTouched[destinationVertexId]) {
                    continue;
                }

                // Calculate the distance to the destination vertex using the current edge
                int newDistanceOverThisEdgeToDestVertex = currDistanceToNode[nodeToHandleId] + Edge.getDist(neighbourEdgeId);

                // If the new calculated distance to the destination vertex is lower as the previously known, update the corresponding data stucutres
                if (newDistanceOverThisEdgeToDestVertex < currDistanceToNode[destinationVertexId]) {
                    currDistanceToNode[destinationVertexId] = newDistanceOverThisEdgeToDestVertex;
                    previousNode[destinationVertexId] = nodeToHandleId;
                    vertexHeap.add(destinationVertexId);
                }

            }
        }

        if(routeFound) {
            // Here, we are done with dijkstra but need to gather all relevant data from the resulting data structures
            List<Integer> path = new ArrayList<>();
            int currNodeUnderInvestigation = destNodeIdx;

            path.add(destNodeIdx);
            while (currNodeUnderInvestigation != startNodeIdx) {
                int previousNodeIdx = previousNode[currNodeUnderInvestigation];
                path.add(previousNodeIdx);
                currNodeUnderInvestigation = previousNodeIdx;
            }

            // Reverse order of path and save it to array
            Collections.reverse(path);
            long stopTime = System.nanoTime();

            return new RoutingResult(path, currDistanceToNode[destNodeIdx], (double) (stopTime - startTime) / 1000000);
        } else {
            long stopTime = System.nanoTime();
            return new RoutingResult((double) (stopTime - startTime) / 1000000, false);
        }
    }
}
