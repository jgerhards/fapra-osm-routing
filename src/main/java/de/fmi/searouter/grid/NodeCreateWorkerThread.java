package de.fmi.searouter.grid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

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
        for (BigDecimal lat : latList) {
            for (BigDecimal longitude = BigDecimal.valueOf(180); longitude.compareTo(longEnd) > 0; longitude = longitude.subtract(coordinateStepLong)) {

                if (!GridCreator.isPointOnWater(lat.doubleValue(), longitude.doubleValue())) {
                    continue;
                }

                GridNode node = new GridNode(lat.doubleValue(), longitude.doubleValue());

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
