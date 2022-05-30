package de.fmi.searouter.router;

import de.fmi.searouter.grid.Edge;
import de.fmi.searouter.grid.Grid;
import de.fmi.searouter.grid.Node;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DijkstraRouter implements Router {

    //protected final int[] currDistanceToNode;
    protected int[] currDistanceToNode;
    private final int[] previousNode;
    private final DijkstraHeap vertexHeap;
    private final boolean[] nodeTouched;

    public DijkstraRouter() {
        this.currDistanceToNode = new int[Node.getSize()];
        this.previousNode = new int[Node.getSize()];
        this.nodeTouched = new boolean[Node.getSize()];
        this.vertexHeap = new DijkstraHeap(this);

        for (int nodeIdx = 0; nodeIdx < Node.getSize(); nodeIdx++) {
            currDistanceToNode[nodeIdx] = Integer.MAX_VALUE;
            previousNode[nodeIdx] = -1;
            nodeTouched[nodeIdx] = false;
        }

    }

    private void resetState() {
        Arrays.fill(currDistanceToNode, Integer.MAX_VALUE);
        Arrays.fill(previousNode, -1);
        Arrays.fill(nodeTouched, false);

        vertexHeap.resetState();
    }

    @Override
    public RoutingResult route(int startNodeIdx, int destNodeIdx) {

        /*resetState();
        currDistanceToNode = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        vertexHeap.add(2);
        vertexHeap.add(3);
        vertexHeap.add(0);
        vertexHeap.add(1);
        vertexHeap.print();

        currDistanceToNode[3] = -1;
        vertexHeap.add(3);
        vertexHeap.print();

        vertexHeap.getNext();
        vertexHeap.print();
        vertexHeap.getNext();
        vertexHeap.print();

        if(true)
            return null;*/

        long startTime = System.nanoTime();
        resetState();

        currDistanceToNode[startNodeIdx] = 0;
        previousNode[startNodeIdx] = startNodeIdx;
        vertexHeap.add(startNodeIdx);

        while (!vertexHeap.isEmpty()) {
            int nodeToHandleId = vertexHeap.getNext();
            nodeTouched[nodeToHandleId] = true;

            // Break early if target node reached
            if (nodeToHandleId == destNodeIdx) {
                break;
            }

            for (int neighbourEdgeId = Grid.offset[nodeToHandleId]; neighbourEdgeId < Grid.offset[nodeToHandleId + 1]; ++neighbourEdgeId) {

                int destinationVertexId = Edge.getDest(neighbourEdgeId);

                // If the edge destination vertex is not in the vertexSetQ
                //if (!vertexSetQ.contains(destinationVertexId)) {
                //    continue;
                //}

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
