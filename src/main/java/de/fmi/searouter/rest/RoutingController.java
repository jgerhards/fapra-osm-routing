package de.fmi.searouter.rest;

import de.fmi.searouter.router.*;
import de.fmi.searouter.dijkstragrid.Grid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route")
public class RoutingController {
    private static boolean useHLRouter;

    public static void setHLRouterUse(boolean useHLRouter) {
        RoutingController.useHLRouter = useHLRouter;
    }

    Router router;

    @PostMapping("")
    public ResponseEntity getRoute(@RequestBody RoutingRequest routingRequest) {
        if(router == null) {
            if(useHLRouter) {
                router = new HubLRouter();
            } else {
                router = new DijkstraRouter();
            }
        }

       int startNodeId = Grid.getNearestGridNodeByCoordinates(routingRequest.getStartPoint().getLatitude(), routingRequest.getStartPoint().getLongitude());
       int destNodeId = Grid.getNearestGridNodeByCoordinates(routingRequest.getEndPoint().getLatitude(), routingRequest.getEndPoint().getLongitude());

        if (startNodeId < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start position is not on the ocean!");
        }

        if (destNodeId < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Destination position is not on the ocean!");
        }

        if (startNodeId == destNodeId) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Destination position is equal to start position!");
        }

        RoutingResult res = router.route(startNodeId, destNodeId);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/test")
    public String getTest() {
        return "testSuccess" ;
    }
}
