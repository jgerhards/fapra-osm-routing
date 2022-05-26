package de.fmi.searouter.router;

import de.fmi.searouter.grid.Edge;
import de.fmi.searouter.grid.Grid;
import de.fmi.searouter.grid.Node;

import java.util.*;

public class DijkstraRouter implements Router {

    private int[] currDistanceToNode;
    private int[] previousNode;
    private Set<Integer> vertexSetQ;

    private void init(int startNodeIdx) {
        currDistanceToNode = new int[Node.getSize()];
        previousNode = new int[Node.getSize()];
        vertexSetQ = new HashSet<>();

        for (int nodeIdx = 0; nodeIdx < Node.getSize(); nodeIdx++) {
            currDistanceToNode[nodeIdx] = Integer.MAX_VALUE;
            previousNode[nodeIdx] = -1;
            vertexSetQ.add(nodeIdx);
        }

        currDistanceToNode[startNodeIdx] = 0;
    }

    @Override
    public RoutingResult route(int startNodeIdx, int destNodeIdx) {
        init(startNodeIdx);

        while (!vertexSetQ.isEmpty()) {
            int nodeToHandle = getVertexWithMinimalDistance(vertexSetQ, currDistanceToNode);

            // Remove the min distance node from the Q-Set
            vertexSetQ.remove(nodeToHandle);

            for (int neighbourEdgeId = Grid.offset[nodeToHandle]; neighbourEdgeId < Grid.offset[nodeToHandle + 1]; ++neighbourEdgeId) {

                int destinationVertexId = Edge.getDest(neighbourEdgeId);

                // If the edge destination vertex is not in the vertexSetQ
                if (!vertexSetQ.contains(destinationVertexId)) {
                    continue;
                }

                // Calculate the distance to the destination vertex using the current edge
                int newDistanceOverThisEdgeToDestVertex = currDistanceToNode[nodeToHandle] + Edge.getDist(neighbourEdgeId);

                // If the new calculated distance to the destination vertex is lower as the previously known, update the corresponding data stucutres
                if (newDistanceOverThisEdgeToDestVertex < currDistanceToNode[destinationVertexId]) {
                        currDistanceToNode[destinationVertexId] = newDistanceOverThisEdgeToDestVertex;
                        previousNode[destinationVertexId] = nodeToHandle;
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

        return new RoutingResult(path, currDistanceToNode[destNodeIdx]);
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
