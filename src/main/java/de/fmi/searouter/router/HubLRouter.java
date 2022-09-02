package de.fmi.searouter.router;

import de.fmi.searouter.hublabeldata.HubLEdges;
import de.fmi.searouter.hublabeldata.HubLNodes;
import de.fmi.searouter.hublablecreation.CHDijkstraHeap;
import de.fmi.searouter.utils.IntStack;
import de.fmi.searouter.utils.IntersectionHelper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HubLRouter implements Router{
    private CHDijkstraHeap heap;
    private IntStack resultNodes;
    private IntStack edgesRight;
    private IntStack edgesLeft;
    private IntStack edgeCalcStack;

    public void reset() {
        heap.reset();
        resultNodes.clear();
        edgesRight.clear();
        edgesLeft.clear();
        edgeCalcStack.clear();
    }

    public HubLRouter() {
        heap = new CHDijkstraHeap();
        resultNodes = new IntStack(5000);
        edgesRight = new IntStack(500);
        edgesLeft = new IntStack(500);
        edgeCalcStack = new IntStack(10);
    }

    @Override
    public RoutingResult route(int startNodeIdx, int destNodeIdx) {
        System.out.println("ttt: hub label router called");
        long startTime = System.nanoTime();
        boolean startHasLabels = HubLNodes.nodeHasLabels(startNodeIdx);
        boolean destHasLabels = HubLNodes.nodeHasLabels(destNodeIdx);

        RoutingResult result = new RoutingResult();
        if(startHasLabels && destHasLabels) {
            result = route2Lbl(startNodeIdx, destNodeIdx);
        }

        long stopTime = System.nanoTime();
        result.setCalculationTimeInMs((double) (stopTime - startTime) / 1000000);
        return result;
    }

    private RoutingResult route2Lbl(int startId, int destId) {
        int currOffsetA = HubLNodes.getLabelOffset(startId);
        int currOffsetB = HubLNodes.getLabelOffset(destId);
        int endOffsetA = HubLNodes.getLabelOffset(startId + 1);
        int endOffsetB = HubLNodes.getLabelOffset(destId + 1);
        int currDistance = Integer.MAX_VALUE;
        int idxA = -1;
        int idxB = -1;
        int highestLvlNode = -1;

        if(currOffsetA == endOffsetA || currOffsetB == endOffsetB) {
            //no route
            return new RoutingResult();
        }
        int nodeA = HubLNodes.getLabelNode(currOffsetA);
        int nodeB = HubLNodes.getLabelNode(currOffsetB);

        while (currOffsetA < endOffsetA && currOffsetB < endOffsetB) {
            if(nodeA == nodeB) {
                int tmpDist = HubLNodes.getLabelDist(currOffsetA) + HubLNodes.getLabelDist(currOffsetB);
                if(tmpDist < currDistance) {
                    highestLvlNode = nodeA;
                    currDistance = tmpDist;
                    idxA = currOffsetA;
                    idxB = currOffsetB;
                }
                currOffsetA++;
                currOffsetB++;
                nodeA = HubLNodes.getLabelNode(currOffsetA);
                nodeB = HubLNodes.getLabelNode(currOffsetB);
            } else if(nodeA > nodeB) {
                currOffsetB++;
                nodeB = HubLNodes.getLabelNode(currOffsetB);
            } else {  //A < B
                currOffsetA++;
                nodeA = HubLNodes.getLabelNode(currOffsetA);
            }
        }

        //double actualDistance = IntersectionHelper.getDistance(HubLNodes.getLat(startId), HubLNodes.getLong(startId),
        //        HubLNodes.getLat(destId), HubLNodes.getLong(destId));
        if(highestLvlNode == -1) { //no common label found
            return new RoutingResult();
        }

        boolean highestLvlRight;
        //calculate right side of path to max lvl node
        if(nodeB != highestLvlNode) {
            highestLvlRight = false;
            int nextEdge = HubLNodes.getLabelEdge(idxB);
            int nextNode = HubLEdges.getDest(nextEdge);
            edgesRight.push(nextEdge);
            getEdges(nextNode, highestLvlNode, edgesRight);
        } else {
            highestLvlRight = true;
        }

        boolean highestLvlLeft;
        if(nodeA != highestLvlNode) {
            highestLvlLeft = false;
            int nextEdge = HubLNodes.getLabelEdge(idxA);
            int nextNode = HubLEdges.getDest(nextEdge);
            edgesLeft.push(nextEdge);
            getEdges(nextNode, highestLvlNode, edgesLeft);
        } else {
            highestLvlLeft = true;
        }
        calculateRoute(highestLvlLeft, highestLvlRight, startId, destId);

        //todo: here
        List<Integer> path = new LinkedList<>();
        while(!resultNodes.isEmpty()) {
            path.add(resultNodes.popFifo());
        }
        return new RoutingResult(path, currDistance, true);
    }

    private void getEdges(int currentNode, int highestLvlNode, IntStack edgeStack) {
        while(currentNode != highestLvlNode) {
            int startIdx = HubLNodes.getLabelOffset(currentNode);
            int endIdx = HubLNodes.getLabelOffset(currentNode + 1);
            int labelIdx = Arrays.binarySearch(HubLNodes.getLabelNode(), startIdx, endIdx, highestLvlNode);
            int edgeId = HubLNodes.getLabelEdge(labelIdx);
            currentNode = HubLEdges.getDest(edgeId);
            edgeStack.push(edgeId);
        }
    }

    private void calculateRoute(boolean highestLvlLeft, boolean highestLvlRight, int start, int dest) {
        resultNodes.push(start);
        while(!edgesLeft.isEmpty()) {
            edgeCalcStack.push(edgesLeft.pop());
            while(!edgeCalcStack.isEmpty()) {
                int currentEdge = edgeCalcStack.pop();
                if(HubLEdges.isShortcut(currentEdge)) {
                    edgeCalcStack.push(HubLEdges.getFirstShortcut(currentEdge));
                    edgeCalcStack.push(HubLEdges.getSecondShortcut(currentEdge));
                } else {
                    resultNodes.push(HubLEdges.getDest(currentEdge));
                }
            }
        }
        //correct for highest lvl being added twice otherwise (one from left, once from right)
        resultNodes.pop();

        while(!edgesRight.isEmpty()) {
            edgeCalcStack.push(edgesRight.popFifo());
            while(!edgeCalcStack.isEmpty()) {
                int currentEdge = edgeCalcStack.pop();
                if(HubLEdges.isShortcut(currentEdge)) {
                    edgeCalcStack.push(HubLEdges.getSecondShortcut(currentEdge));
                    edgeCalcStack.push(HubLEdges.getFirstShortcut(currentEdge));
                } else {
                    resultNodes.push(HubLEdges.getDest(currentEdge));
                }
            }
        }

        resultNodes.push(dest);
    }


    /*
            int topLvlNode = -1;
        calcStack.push(startId);
        calcStack.push(destId);

        while(!calcStack.isEmpty()) {
            int destNode = calcStack.pop();
            int startNode = calcStack.pop();
            int currOffsetA = HubLNodes.getLabelOffset(startNode);
            int currOffsetB = HubLNodes.getLabelOffset(destNode);
            int endOffsetA = HubLNodes.getLabelOffset(startNode + 1);
            int endOffsetB = HubLNodes.getLabelOffset(destNode + 1);
            int currDistance = Integer.MAX_VALUE;
            int idxA;
            int idxB;
            boolean found;

            if(currOffsetA == endOffsetA || currOffsetB == endOffsetB) {
                return new RoutingResult();
            }
            int nodeA = HubLNodes.getLabelNode(currOffsetA);
            int nodeB = HubLNodes.getLabelNode(currOffsetB);

            while (currOffsetA < endOffsetA && currOffsetB < endOffsetB) {
                if(nodeA == nodeB) {
                    int tmpDist = HubLNodes.getLabelDist(currOffsetA) + HubLNodes.getLabelDist(currOffsetB);
                    if(tmpDist < currDistance) {
                        found = true;
                        currDistance = tmpDist;
                        idxA = currOffsetA;
                        idxB = currOffsetB;
                    }
                    currOffsetA++;
                    currOffsetB++;
                }
            }
        }

     */
}
