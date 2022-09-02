package de.fmi.searouter;

import de.fmi.searouter.dijkstragrid.Grid;
import de.fmi.searouter.hublabeldata.HubLNodes;
import de.fmi.searouter.hublabeldata.HubLStore;
import de.fmi.searouter.rest.RoutingController;
import de.fmi.searouter.router.RoutingResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SearouterApplication {
	private static boolean USE_HUB_LABEL_ROUTER = true;
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

		/*for (int i = 0; i < 600000; i++) {
			if(HubLNodes.nodeHasLabels(i)) {
				System.out.println("ttt: coord: " + HubLNodes.getLat(i) + ", " + HubLNodes.getLong(i));
			}
		}*/
		SpringApplication.run(SearouterApplication.class, args);
	}

}