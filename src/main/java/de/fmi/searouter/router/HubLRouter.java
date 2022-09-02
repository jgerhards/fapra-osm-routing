package de.fmi.searouter.router;

import de.fmi.searouter.hublabeldata.HubLEdges;
import de.fmi.searouter.hublabeldata.HubLNodes;
import de.fmi.searouter.hublablecreation.CHDijkstraHeap;
import de.fmi.searouter.utils.IntStack;
import de.fmi.searouter.utils.OrderedIntSet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HubLRouter implements Router{
    private final IntStack resultNodes;
    private final IntStack edgesRight;
    private final IntStack edgesLeft;
    private final IntStack edgeCalcStack;

    private final CHDijkstraHeap heap;
    private final CHDijkstraHeap labelHeap;
    private final OrderedIntSet labelLeft;
    private final OrderedIntSet distLeft;
    private final OrderedIntSet edgeLeft;
    private final OrderedIntSet addInfoLeft;
    private final OrderedIntSet labelRight;
    private final OrderedIntSet distRight;
    private final OrderedIntSet edgeRight;
    private final OrderedIntSet addInfoRight;

    public void reset() {
        heap.reset();
        resultNodes.clear();
        edgesRight.clear();
        edgesLeft.clear();
        edgeCalcStack.clear();
        labelLeft.clear();
        distLeft.clear();
        edgeLeft.clear();
        labelRight.clear();
        distRight.clear();
        edgeRight.clear();
        addInfoLeft.clear();
        addInfoRight.clear();
    }

    public HubLRouter() {
        heap = new CHDijkstraHeap(1000, 1000);
        labelHeap = new CHDijkstraHeap(1000, 1000);
        resultNodes = new IntStack(5000);
        edgesRight = new IntStack(500);
        edgesLeft = new IntStack(500);
        edgeCalcStack = new IntStack(10);
        labelLeft = new OrderedIntSet(true, 1000, 1000);
        distLeft = new OrderedIntSet(false, 1000, 1000);
        edgeLeft = new OrderedIntSet(false, 1000, 1000);
        addInfoLeft = new OrderedIntSet(false, 1000, 1000);
        labelRight = new OrderedIntSet(true, 1000, 1000);
        distRight = new OrderedIntSet(false, 1000, 1000);
        edgeRight = new OrderedIntSet(false, 1000, 1000);
        addInfoRight = new OrderedIntSet(false, 1000, 1000);
    }

    @Override
    public RoutingResult route(int startNodeIdx, int destNodeIdx) {
        System.out.println("ttt: hub label router called");
        long startTime = System.nanoTime();
        reset();
        boolean startHasLabels = HubLNodes.nodeHasLabels(startNodeIdx);
        boolean destHasLabels = HubLNodes.nodeHasLabels(destNodeIdx);

        RoutingResult result = new RoutingResult();
        if(startHasLabels && destHasLabels) {
            result = route2Lbl(startNodeIdx, destNodeIdx);
        } else if (startHasLabels) {

        } else if (destHasLabels) {

        } else {
            route0Lbl(startNodeIdx, destNodeIdx);
        }

        long stopTime = System.nanoTime();
        result.setCalculationTimeInMs((double) (stopTime - startTime) / 1000000);
        return result;
    }

    private RoutingResult route0Lbl(int startId, int destId) {
        calcTempLabels(startId, true);
        calcTempLabels(destId, false);

        //compare labels
        int leftSize = labelLeft.size();
        int rightSize = labelRight.size();
        if(leftSize == 0 || rightSize == 0) {
            return new RoutingResult();
        }
        int leftIdx = 0;
        int rightIdx = 0;
        int currDistance = Integer.MAX_VALUE;
        int idxLeft = -1;
        int idxRight = -1;
        int highestLvlNode = -1;

        int leftNode = labelLeft.get(0);
        int rightNode = labelRight.get(0);
        while(leftIdx < leftSize && rightIdx < rightSize) {
            if(leftNode == rightNode) {
                int tmpDist = distLeft.get(leftIdx) + distRight.get(rightIdx);
                if(tmpDist < currDistance) {
                    highestLvlNode = leftNode;
                    currDistance = tmpDist;
                    idxLeft = leftIdx;
                    idxRight = rightIdx;
                }
                leftIdx++;
                rightIdx++;
                leftNode = HubLNodes.getLabelNode(leftIdx);
                rightNode = HubLNodes.getLabelNode(rightIdx);
            } else if(leftNode > rightNode) {
                rightIdx++;
                rightNode = HubLNodes.getLabelNode(rightIdx);
            } else {  //A < B
                leftIdx++;
                leftNode = HubLNodes.getLabelNode(leftIdx);
            }
        }

        if(highestLvlNode == -1) {
            return new RoutingResult();
        }

        if(HubLNodes.getLvl(highestLvlNode) >= 3) {  //todo: no magic numbers if possible
            int leftLabelNode = addInfoLeft.get(idxLeft);
            int idx = labelLeft.getIdx(leftLabelNode);
            int nextEdge = addInfoLeft.get(idx);
            if(nextEdge != -1) {
                edgesLeft.push(nextEdge);
            }
            nextEdge = edgeLeft.get(idx);
            // in this case, left node has to have at least one edge to get to highest lvl node
            edgesLeft.push(nextEdge);
            int firstEdge = edgeLeft.get(idxLeft);
            edgesLeft.push(firstEdge);
            getEdges(HubLEdges.getDest(firstEdge), highestLvlNode, edgesLeft);

            int rightLabelNode = addInfoRight.get(idxRight);
            idx = labelRight.getIdx(rightLabelNode);
            nextEdge = addInfoRight.get(idx);
            if(nextEdge != -1) {
                edgesRight.push(nextEdge);
            }
            nextEdge = edgeRight.get(idx);
            // in this case, right node has to have at least one edge to get to highest lvl node
            edgesRight.push(nextEdge);
            firstEdge = edgeRight.get(idxRight);
            edgesRight.push(firstEdge);
            getEdges(HubLEdges.getDest(firstEdge), highestLvlNode, edgesRight);
        } else {
            int idx = labelLeft.getIdx(highestLvlNode);
            int nextEdge = addInfoLeft.get(idx);
            if(nextEdge != -1) {
                edgesLeft.push(nextEdge);
            }
            nextEdge = edgeLeft.get(idx);
            if(nextEdge != -1) {
                edgesLeft.push(nextEdge);
            }
            idx = labelRight.getIdx(highestLvlNode);
            nextEdge = addInfoRight.get(idx);
            if(nextEdge != -1) {
                edgesRight.push(nextEdge);
            }
            nextEdge = edgeRight.get(idx);
            if(nextEdge != -1) {
                edgesRight.push(nextEdge);
            }
        }

        calculateRoute(startId, destId);
        List<Integer> path = new LinkedList<>();
        while(!resultNodes.isEmpty()) {
            path.add(resultNodes.popFifo());
        }
        return new RoutingResult(path, currDistance, true);
    }

    private void calcTempLabels(int nodeId, boolean isLeftNode) {
        labelHeap.reset();
        heap.reset();
        //make sure we only have to check this once
        OrderedIntSet label;
        OrderedIntSet dist;
        OrderedIntSet edge;
        OrderedIntSet addInfo;
        if(isLeftNode) {
            label = labelLeft;
            dist = distLeft;
            edge = edgeLeft;
            addInfo = addInfoLeft;
        } else {
            label = labelRight;
            dist = distRight;
            edge = edgeRight;
            addInfo = addInfoRight;
        }
        int maxNumOfLabels = 0;
        //first step outside of loop
        label.insertTail(nodeId);
        dist.insertTail(0);
        edge.insertTail(-1);
        addInfo.insertTail(-1);
        int edgesStart = HubLNodes.getEdgeOffset(nodeId);
        int edgesStop = HubLNodes.getEdgeOffset(nodeId + 1);
        for (int i = edgesStart; i < edgesStop; i++) {
            int edgeId = HubLNodes.getEdge(i);
            int destNode = HubLEdges.getDest(edgeId);
            int edgeDist = HubLEdges.getDist(edgeId);

            int insertIdx = label.getIdx(destNode);
            if(insertIdx < 0) {
                if(HubLNodes.nodeHasLabels(destNode)) {
                    labelHeap.add(destNode, edgeDist);
                    maxNumOfLabels += HubLNodes.getLabelOffset(destNode + 1) - HubLNodes.getLabelOffset(destNode);
                } else {
                    heap.add(destNode, edgeDist);
                }
                insertIdx = (insertIdx + 1) * (-1);
                label.insertAtIdx(destNode, insertIdx);
                dist.insertAtIdx(edgeDist, insertIdx);
                edge.insertAtIdx(edgeId, insertIdx);
                // no previous edge
                addInfo.insertTail(-1);
            } else {
                int oldDist = dist.get(insertIdx);
                if(oldDist > edgeDist) {
                    if(HubLNodes.nodeHasLabels(destNode)) {
                        labelHeap.add(destNode, edgeDist);
                    } else {
                        heap.add(destNode, edgeDist);
                    }
                    dist.updateValue(edgeDist, insertIdx);
                    edge.updateValue(edgeId, insertIdx);
                    //addInfo is -1 anyway, no need to update
                }
            }
        }

        while(!heap.isEmpty()) {
            int currNode = heap.getNext();
            int currNodeIdx = label.getIdx(currNode);
            int currNodeDist = dist.get(currNodeIdx);
            int currNodePrevEdge = addInfo.get(currNodeIdx);
            edgesStart = HubLNodes.getEdgeOffset(currNode);
            edgesStop = HubLNodes.getEdgeOffset(currNode + 1);
            for (int i = edgesStart; i < edgesStop; i++) {
                int edgeId = HubLNodes.getEdge(i);
                int destNode = HubLEdges.getDest(edgeId);
                int edgeDist = HubLEdges.getDist(edgeId);

                int insertIdx = label.getIdx(destNode);
                if(insertIdx < 0) {
                    //new node found
                    maxNumOfLabels += HubLNodes.getLabelOffset(destNode + 1) - HubLNodes.getLabelOffset(destNode);
                    insertIdx = (insertIdx + 1) * (-1);
                    label.insertAtIdx(destNode, insertIdx);
                    int initialDist = currNodeDist + edgeDist;
                    //must be at least lvl 2 --> has labels
                    labelHeap.add(destNode, initialDist);
                    dist.insertAtIdx(initialDist, insertIdx);
                    edge.insertAtIdx(edgeId, insertIdx);
                    addInfo.insertAtIdx(currNodePrevEdge, insertIdx);
                } else {
                    int prevDist = dist.get(insertIdx);
                    int newDist = currNodeDist + edgeDist;
                    if(prevDist > newDist) {
                        // dest node has to have lvl >= 2
                        labelHeap.add(destNode, newDist);
                        dist.updateValue(newDist, insertIdx);
                        edge.updateValue(edgeId, insertIdx);
                        addInfo.updateValue(currNodePrevEdge, insertIdx);
                    }
                }
            }
        }

        //insert all labels
        label.makeSpace(maxNumOfLabels);
        dist.makeSpace(maxNumOfLabels);
        edge.makeSpace(maxNumOfLabels);
        addInfo.makeSpace(maxNumOfLabels);

        while(!labelHeap.isEmpty()) {
            int labelNode = labelHeap.getNext();
            int nodeDist = dist.get(label.getIdx(labelNode));
            int endIdx = HubLNodes.getLabelOffset(labelNode + 1);

            for (int currIdx = HubLNodes.getLabelOffset(labelNode); currIdx < endIdx; currIdx++) {
                int currLabelNode = HubLNodes.getLabelNode(currIdx);
                int currLabelIdx = label.getIdx(currLabelNode);

                if(currLabelIdx < 0) {
                    currLabelIdx = (currLabelIdx + 1) * (-1);
                    int distance = nodeDist + HubLNodes.getLabelDist(currIdx);
                    label.insertAtIdx(currLabelNode, currLabelIdx);
                    dist.insertAtIdx(distance, currLabelIdx);
                    edge.insertAtIdx(HubLNodes.getLabelEdge(currIdx), currLabelIdx);
                    addInfo.insertAtIdx(labelNode, currLabelIdx);
                } else {
                    int newDistance = nodeDist + HubLNodes.getLabelDist(currIdx);
                    int oldDistance = dist.get(currLabelIdx);
                    if(oldDistance > newDistance) {
                        dist.updateValue(newDistance, currLabelIdx);
                        edge.updateValue(HubLNodes.getLabelEdge(currIdx), currLabelIdx);
                        addInfo.updateValue(labelNode, currLabelIdx);
                    }
                }
            }
        }
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
        calculateRoute(startId, destId);

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

    private void calculateRoute(int start, int dest) {
        resultNodes.push(start);
        while(!edgesLeft.isEmpty()) {
            edgeCalcStack.push(edgesLeft.popFifo());
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
        //correct for highest lvl being added twice otherwise (one from left, once from right)
        resultNodes.pop();

        while(!edgesRight.isEmpty()) {
            edgeCalcStack.push(edgesRight.pop());
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
