package de.fmi.searouter.router;

import de.fmi.searouter.hublabeldata.HubLEdges;
import de.fmi.searouter.hublabeldata.HubLNodes;
import de.fmi.searouter.utils.DistanceHeap;
import de.fmi.searouter.utils.IntStack;
import de.fmi.searouter.utils.OrderedIntSet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Router based on the hub label algorithm. In order to use less memory, this router uses a combination of hub labels
 * and contraction hierarchy (on the two lowest levels) routing. This router can only be used if exactly two
 * levels of nodes do not contain hub labels (instead information on edges to higher level nodes has to be provided).
 * However, while it is not able to be used for other cases, it is very efficient in this case.
 */
public class HubLRouter implements Router{
    //**general note:** this router contains some parts in the code which may look weird or like code clones.
    //in most cases, this will be due to an optimization which may eliminate some checks which would appear in every
    //iteration otherwise.

    //nodes on the resulting path (if one is found)
    private final IntStack resultNodes;
    //edges for the left (start to highest lvl node) and right (dest to highest lvl node) sides of the path
    private final IntStack edgesRight;
    private final IntStack edgesLeft;
    //stack used while calculating which edges are on the path
    private final IntStack edgeCalcStack;

    //while calculating temporary labels, contains nodes which are to be expanded but do not contain labels
    private final DistanceHeap heap;
    //while calculating temporary labels, contains nodes which are to be expanded and contains labels
    private final DistanceHeap labelHeap;

    //data structures for left side (start to highest lvl node), used for temporary labels
    //node ids of the labels
    private final OrderedIntSet labelLeft;
    //distance from the origin node to the one at the same position in labelLeft
    private final OrderedIntSet distLeft;
    //first edge on the way from the origin node to the one at the same position in labelLeft
    private final OrderedIntSet edgeLeft;
    //additional info, may contain a node (if label at this idx was created from a label) or
    // an edge (label created from a contraction hierarchy dijkstra)
    private final OrderedIntSet addInfoLeft;

    //same data structures for right side (dest to highest lvl node)
    private final OrderedIntSet labelRight;
    private final OrderedIntSet distRight;
    private final OrderedIntSet edgeRight;
    private final OrderedIntSet addInfoRight;

    /**
     * Reset the state of the router. Internal data structures are cleared so they can be reused to precess a new
     * request.
     */
    private void reset() {
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

    /**
     * Constructor. Initializes all data structures used by the router.
     */
    public HubLRouter() {
        heap = new DistanceHeap(1000, 1000);
        labelHeap = new DistanceHeap(1000, 1000);
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

    /**
     * Calculate the shortest path from one start node to a destination node. In order to find this route,
     * an implementation using hub labels with two levels of contraction hierarchies is used.
     * @param startNodeIdx The index of the start node
     * @param destNodeIdx The index of the destination node
     * @return a shortest route between start and destination node
     */
    @Override
    public RoutingResult route(int startNodeIdx, int destNodeIdx) {
        long startTime = System.nanoTime();

        RoutingResult result = routeGeneral(startNodeIdx, destNodeIdx);

        //set calculation time in result
        long stopTime = System.nanoTime();
        result.setCalculationTimeInMs((double) (stopTime - startTime) / 1000000);
        return result;
    }


    /**
     * Find a route between two nodes. This function can be used if both side nodes contain labels.
     * @param startId The index of the start node
     * @param destId The index of the destination node
     * @return a shortest route between start and destination node
     */
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

        //find the best common label
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

        //calculate right side of path to max lvl node
        if(destId != highestLvlNode) {
            int nextEdge = HubLNodes.getLabelEdge(idxB);
            int nextNode = HubLEdges.getDest(nextEdge);
            edgesRight.push(nextEdge);
            getEdges(nextNode, highestLvlNode, edgesRight);
        }

        //calculate left side of path to max lvl node
        if(startId != highestLvlNode) {
            int nextEdge = HubLNodes.getLabelEdge(idxA);
            int nextNode = HubLEdges.getDest(nextEdge);
            edgesLeft.push(nextEdge);
            getEdges(nextNode, highestLvlNode, edgesLeft);
        }

        calculateRoute(startId, destId);

        //add nodes to path for result
        List<Integer> path = new LinkedList<>();
        while(!resultNodes.isEmpty()) {
            path.add(resultNodes.popFifo());
        }
        return new RoutingResult(path, currDistance, true);
    }

    /**
     * Find edges on the way from a node to another node with a higher level.
     * @param currentNode the initial node
     * @param highestLvlNode the node with the highest level on the way
     * @param edgeStack the stack to which the edges should be added
     */
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

    /**
     * Determine the nodes on the path between two nodes. This is based on other data structures
     * containing information on the edges on the left and the right path.
     * @param start the id of the start node
     * @param dest the id of the destination node
     */
    private void calculateRoute(int start, int dest) {
        resultNodes.push(start);
        while(!edgesLeft.isEmpty()) {
            edgeCalcStack.push(edgesLeft.popFifo());
            while(!edgeCalcStack.isEmpty()) {
                int currentEdge = edgeCalcStack.pop();
                if(HubLEdges.isShortcut(currentEdge)) {
                    //order of push is important
                    edgeCalcStack.push(HubLEdges.getSecondShortcut(currentEdge));
                    edgeCalcStack.push(HubLEdges.getFirstShortcut(currentEdge));
                } else {
                    resultNodes.push(HubLEdges.getDest(currentEdge));
                }
            }
        }
        //correct for highest lvl node being added twice otherwise (one from left, once from right)
        resultNodes.pop();

        while(!edgesRight.isEmpty()) {
            edgeCalcStack.push(edgesRight.pop());
            while(!edgeCalcStack.isEmpty()) {
                int currentEdge = edgeCalcStack.pop();
                if(HubLEdges.isShortcut(currentEdge)) {
                    //order of push is important
                    edgeCalcStack.push(HubLEdges.getFirstShortcut(currentEdge));
                    edgeCalcStack.push(HubLEdges.getSecondShortcut(currentEdge));
                } else {
                    resultNodes.push(HubLEdges.getDest(currentEdge));
                }
            }
        }
        resultNodes.push(dest);
    }

    private RoutingResult routeGeneral(int startNodeIdx, int destNodeIdx) {
        reset();
        boolean startHasLabels = HubLNodes.nodeHasLabels(startNodeIdx);
        boolean destHasLabels = HubLNodes.nodeHasLabels(destNodeIdx);

        RoutingResult result;
        if(startHasLabels && destHasLabels) {
            result = route2Lbl(startNodeIdx, destNodeIdx);
        } else if (!startHasLabels && !destHasLabels) {
            result = route0LblGeneral(startNodeIdx, destNodeIdx);
        } else if (startHasLabels){
            result = routeLeftLblGeneral(startNodeIdx, destNodeIdx);
        } else {
            result = routeRightLblGeneral(startNodeIdx, destNodeIdx);
        }

        return result;
    }

    /**
     * Find a route between two nodes. This function can be used if the right side node does not contain labels,
     * but the left side node does. This function is mirrored with routeRightLbl, but they are separate to
     * reduce total calculation time by being able to disregard some additional checks.
     * @param startId The index of the start node
     * @param destId The index of the destination node
     * @return a shortest route between start and destination node
     */
    private RoutingResult routeLeftLblGeneral(int startId, int destId) {
        calcTempLabelsGeneral(destId, false);
        int leftIdx = HubLNodes.getLabelOffset(startId);
        int leftMaxIdx = HubLNodes.getLabelOffset(startId + 1);
        int rightIdx = 0;
        int rightSize = labelRight.size();

        if(leftIdx == leftMaxIdx || rightSize == 0) {
            //no route
            return new RoutingResult();
        }

        //add one dummy output to prevent early evaluation of get from causing exceptions
        labelRight.insertTail(Integer.MAX_VALUE);

        //find the best common label (if there is one)
        int currDistance = Integer.MAX_VALUE;
        int idxLeft = -1;
        int idxRight = -1;
        int highestLvlNode = -1;

        int leftNode = HubLNodes.getLabelNode(leftIdx);
        int rightNode = labelRight.get(0);
        while(leftIdx < leftMaxIdx && rightIdx < rightSize) {
            if(leftNode == rightNode) {
                int tmpDist = HubLNodes.getLabelDist(leftIdx) + distRight.get(rightIdx);
                if(tmpDist < currDistance) {
                    highestLvlNode = leftNode;
                    currDistance = tmpDist;
                    idxLeft = leftIdx;
                    idxRight = rightIdx;
                }
                leftIdx++;
                rightIdx++;
                leftNode = HubLNodes.getLabelNode(leftIdx);
                rightNode = labelRight.get(rightIdx);
            } else if(leftNode > rightNode) {
                rightIdx++;
                rightNode = labelRight.get(rightIdx);
            } else {  //A < B
                leftIdx++;
                leftNode = HubLNodes.getLabelNode(leftIdx);
            }
        }

        if(highestLvlNode == -1) {
            //no common label, so no route
            return new RoutingResult();
        }

        //find edges on left side
        if(startId != highestLvlNode) {
            int nextEdge = HubLNodes.getLabelEdge(idxLeft);
            int nextNode = HubLEdges.getDest(nextEdge);
            edgesLeft.push(nextEdge);
            getEdges(nextNode, highestLvlNode, edgesLeft);
        }

        //find edges on right side
        int info = addInfoRight.get(idxRight);
        if(info < -1) {
            //additional info contains a node
            info = (info + 2) * (-1);
            addEdgesGeneral(edgesRight, info, destId, false);
            int firstEdge = edgeRight.get(idxRight);
            edgesRight.push(firstEdge);
            getEdges(HubLEdges.getDest(firstEdge), highestLvlNode, edgesRight);
        } else if(info != -1) { //if this is false, the highest lvl node is the start node
            //additional info contains an edge (or -1 if no edge relevant for this label)
            addEdgesGeneral(edgesRight, info, destId, false);
        }

        calculateRoute(startId, destId);

        //add route to path
        List<Integer> path = new LinkedList<>();
        while(!resultNodes.isEmpty()) {
            path.add(resultNodes.popFifo());
        }
        return new RoutingResult(path, currDistance, true);
    }

    /**
     * Find a route between two nodes. This function can be used if the left side node does not contain labels,
     * but the right side node does. This function is mirrored with routeLeftLbl, but they are separate to
     * reduce total calculation time by being able to disregard some additional checks.
     * @param startId The index of the start node
     * @param destId The index of the destination node
     * @return a shortest route between start and destination node
     */
    private RoutingResult routeRightLblGeneral(int startId, int destId) {
        calcTempLabelsGeneral(startId, true);
        int rightIdx = HubLNodes.getLabelOffset(destId);
        int rightMaxIdx = HubLNodes.getLabelOffset(destId + 1);
        int leftIdx = 0;
        int leftSize = labelLeft.size();

        if(rightIdx == rightMaxIdx || leftSize == 0) {
            //no route
            return new RoutingResult();
        }

        //add one dummy output to prevent early evaluation of get from causing exceptions
        labelLeft.insertTail(Integer.MAX_VALUE);

        //find the best common label (if there is one)
        int currDistance = Integer.MAX_VALUE;
        int idxRight = -1;
        int idxLeft = -1;
        int highestLvlNode = -1;

        int rightNode = HubLNodes.getLabelNode(rightIdx);
        int leftNode = labelLeft.get(0);
        while(rightIdx < rightMaxIdx && leftIdx < leftSize) {
            if(leftNode == rightNode) {
                int tmpDist = HubLNodes.getLabelDist(rightIdx) + distLeft.get(leftIdx);
                if(tmpDist < currDistance) {
                    highestLvlNode = leftNode;
                    currDistance = tmpDist;
                    idxLeft = leftIdx;
                    idxRight = rightIdx;
                }
                leftIdx++;
                rightIdx++;
                rightNode = HubLNodes.getLabelNode(rightIdx);
                leftNode = labelLeft.get(leftIdx);
            } else if(rightNode > leftNode) {
                leftIdx++;
                leftNode = labelLeft.get(leftIdx);
            } else {
                rightIdx++;
                rightNode = HubLNodes.getLabelNode(rightIdx);
            }
        }

        if(highestLvlNode == -1) {
            //no common label, so no route
            return new RoutingResult();
        }

        //find edges on right side
        if(destId != highestLvlNode) {
            int nextEdge = HubLNodes.getLabelEdge(idxRight);
            int nextNode = HubLEdges.getDest(nextEdge);
            edgesRight.push(nextEdge);
            getEdges(nextNode, highestLvlNode, edgesRight);
        }

        //find edges on left side
        int info = addInfoLeft.get(idxLeft);
        if(info < -1) {
            //additional info contains a node
            info = (info + 2) * (-1);
            addEdgesGeneral(edgesLeft, info, startId, true);
            int firstEdge = edgeLeft.get(idxLeft);
            edgesLeft.push(firstEdge);
            getEdges(HubLEdges.getDest(firstEdge), highestLvlNode, edgesLeft);
        } else if(info != -1){ //if this is false, the highest lvl node is the start node
            //additional info contains an edge (or -1 if no edge relevant for this label)
            addEdgesGeneral(edgesLeft, info, startId, true);
        }

        calculateRoute(startId, destId);

        //add route to path
        List<Integer> path = new LinkedList<>();
        while(!resultNodes.isEmpty()) {
            path.add(resultNodes.popFifo());
        }
        return new RoutingResult(path, currDistance, true);
    }

    /**
     * Find a route between two nodes. This function can be used if neither side nodes contain labels.
     * @param startId The index of the start node
     * @param destId The index of the destination node
     * @return a shortest route between start and destination node
     */
    private RoutingResult route0LblGeneral(int startId, int destId) {
        calcTempLabelsGeneral(startId, true);
        calcTempLabelsGeneral(destId, false);

        //compare labels
        int leftSize = labelLeft.size();
        int rightSize = labelRight.size();
        if(leftSize == 0 || rightSize == 0) {
            return new RoutingResult();
        }
        //add one dummy output to prevent early evaluation of get from causing exceptions
        labelLeft.insertTail(Integer.MAX_VALUE);
        labelRight.insertTail(Integer.MAX_VALUE);

        //find the best common label (if there is one)
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
                leftNode = labelLeft.get(leftIdx);
                rightNode = labelRight.get(rightIdx);
            } else if(leftNode > rightNode) {
                rightIdx++;
                rightNode = labelRight.get(rightIdx);
            } else {  //A < B
                leftIdx++;
                leftNode = labelLeft.get(leftIdx);
            }
        }

        if(highestLvlNode == -1) {
            //no common label, so no route
            return new RoutingResult();
        }

        //find edges on left side
        int info = addInfoLeft.get(idxLeft);
        if(info < -1) {
            //additional info contains a node
            info = (info + 2) * (-1);
            addEdgesGeneral(edgesLeft, info, startId, true);
            int firstEdge = edgeLeft.get(idxLeft);
            edgesLeft.push(firstEdge);
            getEdges(HubLEdges.getDest(firstEdge), highestLvlNode, edgesLeft);
        } else if(info != -1){ //if this is false, the highest lvl node is the start node
            //additional info contains an edge (or -1 if no edge relevant for this label)
            addEdgesGeneral(edgesLeft, info, startId, true);
        }

        //find edges on right side
        info = addInfoRight.get(idxRight);
        if(info < -1) {
            //additional info contains a node
            info = (info + 2) * (-1);
            addEdgesGeneral(edgesRight, info, destId, false);
            int firstEdge = edgeRight.get(idxRight);
            edgesRight.push(firstEdge);
            getEdges(HubLEdges.getDest(firstEdge), highestLvlNode, edgesRight);
        } else if(info != -1) { //if this is false, the highest lvl node is the start node
            //additional info contains an edge (or -1 if no edge relevant for this label)
            addEdgesGeneral(edgesRight, info, destId, false);
        }

        calculateRoute(startId, destId);

        //add route to path
        List<Integer> path = new LinkedList<>();
        while(!resultNodes.isEmpty()) {
            path.add(resultNodes.popFifo());
        }
        return new RoutingResult(path, currDistance, true);
    }

    private void addEdgesGeneral(IntStack resultStack, int nodeId, int stopId, boolean isLeftNode) {
        //make sure we only have to check this once --> assign structures based on first check
        OrderedIntSet label;
        OrderedIntSet dist;
        OrderedIntSet edge;
        OrderedIntSet addInfo;
        if(isLeftNode) {
            label = labelLeft;
            edge = edgeLeft;
            addInfo = addInfoLeft;
        } else {
            label = labelRight;
            edge = edgeRight;
            addInfo = addInfoRight;
        }

        IntStack tmpStack = new IntStack(20);

        while(nodeId != stopId) {
            int nodeIdx = label.getIdx(nodeId);
            tmpStack.push(edge.get(nodeIdx));
            nodeId = addInfo.get(nodeIdx);
        }

        //we added the edges in the wrong order, now turn them around
        while(!tmpStack.isEmpty()) {
            resultStack.push(tmpStack.popFifo());
        }
    }

    /**
     * Calculate temporary labels for a node which does not contains labels. The results will
     * be stored in the appropriate data structures of the router (either left or right side structures).
     * @param nodeId the node to calculate the labels for
     * @param isLeftNode if true, data is stored for left side, else for right side
     */
    private void calcTempLabelsGeneral(int nodeId, boolean isLeftNode) {
        labelHeap.reset();
        heap.reset();
        //make sure we only have to check this once --> assign structures based on first check
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

        int maxNumOfLabels = 0; //track how many labels have to be added in worst case (most temp labels)
        //first step outside of loop --> fewer calls to heap
        //add initial node with a distance of 0 and no edges which lead to it
        label.insertTail(nodeId);
        dist.insertTail(0);
        edge.insertTail(-1);
        addInfo.insertTail(-1);
        heap.add(nodeId, 0);

        while(!heap.isEmpty()) {
            int currNode = heap.getNext();
            int currNodeIdx = label.getIdx(currNode);
            int currNodeDist = dist.get(currNodeIdx);
            int edgesStart = HubLNodes.getEdgeOffset(currNode);
            int edgesStop = HubLNodes.getEdgeOffset(currNode + 1);
            for (int i = edgesStart; i < edgesStop; i++) {
                int edgeId = HubLNodes.getEdge(i);
                int destNode = HubLEdges.getDest(edgeId);
                int edgeDist = HubLEdges.getDist(edgeId);

                int insertIdx = label.getIdx(destNode);
                if(insertIdx < 0) {
                    //new node found
                    int initialDist = currNodeDist + edgeDist;
                    if(HubLNodes.nodeHasLabels(destNode)) {
                        labelHeap.add(destNode, initialDist);
                        maxNumOfLabels +=
                                HubLNodes.getLabelOffset(destNode + 1) - HubLNodes.getLabelOffset(destNode);
                    } else {
                        heap.add(destNode, initialDist);
                    }
                    insertIdx = (insertIdx + 1) * (-1);
                    label.insertAtIdx(destNode, insertIdx);
                    dist.insertAtIdx(initialDist, insertIdx);
                    edge.insertAtIdx(edgeId, insertIdx);
                    addInfo.insertAtIdx(currNode, insertIdx);
                } else {
                    int prevDist = dist.get(insertIdx);
                    int newDist = currNodeDist + edgeDist;
                    if(prevDist > newDist) {
                        if(HubLNodes.nodeHasLabels(destNode)) {
                            labelHeap.add(destNode, newDist);
                        } else {
                            heap.add(destNode, newDist);
                        }
                        dist.updateValue(newDist, insertIdx);
                        edge.updateValue(edgeId, insertIdx);
                        addInfo.updateValue(currNode, insertIdx);
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
                    //Make label negative to show this is a node
                    addInfo.insertAtIdx((labelNode * (-1)) - 2, currLabelIdx);
                } else {
                    int newDistance = nodeDist + HubLNodes.getLabelDist(currIdx);
                    int oldDistance = dist.get(currLabelIdx);
                    if(oldDistance > newDistance) {
                        dist.updateValue(newDistance, currLabelIdx);
                        edge.updateValue(HubLNodes.getLabelEdge(currIdx), currLabelIdx);
                        addInfo.updateValue((labelNode * (-1)) - 2, currLabelIdx);
                    }
                }
            }
        }
    }

}
