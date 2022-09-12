package de.fmi.searouter;

import de.fmi.searouter.dijkstragrid.Grid;
import de.fmi.searouter.hublabeldata.HubLStore;
import de.fmi.searouter.importdata.LatLong;
import de.fmi.searouter.rest.RoutingController;
import de.fmi.searouter.router.RoutingRequest;
import de.fmi.searouter.router.RoutingResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootApplication
public class SearouterApplication {
	private static boolean USE_HUB_LABEL_ROUTER = false;
	private static boolean IS_TEST_RUN = false;
	private static int TEST_NUM_OF_ROUTES = 100000;
	private static String HUB_LABEL_DATA_FILENAME = "hub_label_data";

	public static void main(String[] args) {
		RoutingController.setHLRouterUse(USE_HUB_LABEL_ROUTER);
		RoutingResult.setHLRouterUse(USE_HUB_LABEL_ROUTER);

		if(USE_HUB_LABEL_ROUTER) {
			HubLStore.readData(HUB_LABEL_DATA_FILENAME);
		} else {
			try {
				Grid.importFmiFile("exported_grid.fmi");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*RoutingController controller = new RoutingController();
		RoutingRequest rq = new RoutingRequest();
		LatLong start = new LatLong();
		start.setLatitude(0.6441010607369009);
		start.setLongitude(-47.063169198655146);
		LatLong dest = new LatLong();
		dest.setLatitude(10.872261259116698);
		dest.setLongitude(-16.410233275905227);
		rq.setStartPoint(start);
		rq.setEndPoint(dest);
		RoutingResult res = controller.getRoutingResult(rq);
		System.exit(123);*/


		if(IS_TEST_RUN) {
			test();
		} else {
			SpringApplication.run(SearouterApplication.class, args);
		}
	}

	private static void test() {
		int testNum = TEST_NUM_OF_ROUTES;
		Random rnd = new Random(123);
		double[] lats = new double[testNum*2];
		double[] longs = new double[testNum*2];
		for (int i = 0; i < testNum*2; i++) {
			lats[i] = (-90.0) + (90.0 - (-90.0)) * rnd.nextDouble();
			longs[i] = (-180.0) + (180.0 - (-180.0)) * rnd.nextDouble();
		}

		List<Double> calcTime = new ArrayList<>(testNum);
		double minCalcTime = Double.MAX_VALUE;
		double maxCalcTime = -1.0;
		int routesFound = 0;
		List<Integer> distances = new ArrayList<>(testNum);
		RoutingController controller = new RoutingController();
		for (int i = 0; i < testNum; i++) {
			RoutingRequest rq = new RoutingRequest();
			LatLong start = new LatLong();
			start.setLatitude(lats[i * 2]);
			start.setLongitude(longs[i * 2]);
			LatLong dest = new LatLong();
			dest.setLatitude(lats[(i * 2) + 1]);
			dest.setLongitude(longs[(i * 2) + 1]);
			rq.setStartPoint(start);
			rq.setEndPoint(dest);

			System.out.print( i + ": From: " + lats[i * 2] + ", " + longs[i * 2] + " To: "
					+ lats[(i * 2) + 1] + ", " + longs[(i * 2) + 1] + " Result: ");
			RoutingResult res = controller.getRoutingResult(rq);
			if(!(res == null) && res.routeFound()) {
				System.out.println(" Distance: " + res.getOverallDistance() + ", Time: " + res.getCalculationTimeInMs());
			} else {
				System.out.println("No route found");
				continue;
			}

			if(res.getCalculationTimeInMs() < minCalcTime) {
				minCalcTime = res.getCalculationTimeInMs();
			} else if(res.getCalculationTimeInMs() > maxCalcTime) {
				maxCalcTime = res.getCalculationTimeInMs();
			}
			calcTime.add(res.getCalculationTimeInMs());
			distances.add(res.getOverallDistance());
			routesFound++;
		}
		double avgCalcTime = calcTime.stream().mapToDouble(a -> a).average().orElse(-1.0);
		double avgDist = distances.stream().mapToInt(a -> a).average().orElse(-1.0);
		System.out.println("\n\n Data: min calc time: " + minCalcTime
				+ ", avg calc time: " + avgCalcTime
				+ ", max calc time: " + maxCalcTime
				+ ", avg distance: \"" + String.format("%.2f", avgDist)
				+ "\", routes found: " + routesFound);
	}

}