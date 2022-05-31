package de.fmi.searouter.grid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Thread which is responsible to perform a point-in-polygon check for {@link GridNode}s
 * in a certain range of value of latitudes.
 */
public class NodeCreateWorkerThread extends Thread {

    public static Semaphore mapGuard = new Semaphore(1, true);

    private List<BigDecimal> latList;

    public NodeCreateWorkerThread() {
        this.latList = new ArrayList<>();
    }

    public void addLatitude(BigDecimal lat) {
        this.latList.add(lat);
    }


    @Override
    public void run() {

        BigDecimal coordinateStepLong = BigDecimal.valueOf(GridCreator.coordinate_step_longitude);

        BigDecimal longEnd = BigDecimal.valueOf(-180);
        // Perform a point-in-polygon test for a given list of latitudes
        for (BigDecimal lat : latList) {
            for (BigDecimal longitude = BigDecimal.valueOf(180); longitude.compareTo(longEnd) > 0; longitude = longitude.subtract(coordinateStepLong)) {

                if (!GridCreator.isPointOnWater(lat.doubleValue(), longitude.doubleValue())) {
                    continue;
                }

                GridNode node = new GridNode(lat.doubleValue(), longitude.doubleValue());

                // Add the node to the central data structures in the GridCreator
                try {
                    mapGuard.acquire();
                    GridCreator.gridNodes.add(node);
                    if (!GridCreator.coordinateNodeStore.containsKey(lat.doubleValue())) {
                        GridCreator.coordinateNodeStore.put(lat.doubleValue(), new HashMap<>());
                    }
                    GridCreator.coordinateNodeStore.get(lat.doubleValue()).put(longitude.doubleValue(), node);
                    mapGuard.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
