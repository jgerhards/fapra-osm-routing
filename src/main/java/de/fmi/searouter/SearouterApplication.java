package de.fmi.searouter;

import de.fmi.searouter.dijkstragrid.Grid;
import de.fmi.searouter.hublabeldata.HubLNodes;
import de.fmi.searouter.hublabeldata.HubLStore;
import de.fmi.searouter.importdata.LatLong;
import de.fmi.searouter.rest.RoutingController;
import de.fmi.searouter.router.RoutingRequest;
import de.fmi.searouter.router.RoutingResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

@SpringBootApplication
public class SearouterApplication {
	private static boolean USE_HUB_LABEL_ROUTER = true;
	private static boolean IS_TEST_RUN = true;
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

		if(IS_TEST_RUN) {
			test();
		} else {
			SpringApplication.run(SearouterApplication.class, args);
		}

		/*for (int i = 0; i < 600000; i++) {
			if(!HubLNodes.nodeHasLabels(i)) {
				System.out.println("ttt: coord: " + HubLNodes.getLat(i) + ", " + HubLNodes.getLong(i));
			}
		}*/
	}

	private static void test() {
		int testNum = 1000;
		Random rnd = new Random(123);
		double[] lats = new double[testNum*2];
		double[] longs = new double[testNum*2];
		for (int i = 0; i < testNum*2; i++) {
			lats[i] = (-90.0) + (90.0 - (-90.0)) * rnd.nextDouble();
			longs[i] = (-180.0) + (180.0 - (-180.0)) * rnd.nextDouble();
		}

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
			}
		}
	}

}