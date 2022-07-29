package de.fmi.searouter.rest;

import de.fmi.searouter.router.RoutingRequest;
import de.fmi.searouter.dijkstragrid.Grid;
import de.fmi.searouter.router.DijkstraRouter;
import de.fmi.searouter.router.RoutingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route")
public class RoutingController {

    @Autowired
    DijkstraRouter router;

    @PostMapping("")
    public ResponseEntity getRoute(@RequestBody RoutingRequest routingRequest) {

       int startNodeId = Grid.getNearestGridNodeByCoordinates(routingRequest.getStartPoint().getLatitude(), routingRequest.getStartPoint().getLongitude());
       int destNodeId = Grid.getNearestGridNodeByCoordinates(routingRequest.getEndPoint().getLatitude(), routingRequest.getEndPoint().getLongitude());

        if (startNodeId < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start position is not on the ocean!");
        }

        if (destNodeId < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Destination position is not on the ocean!");
        }

        RoutingResult res = router.route(startNodeId, destNodeId);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/test")
    public String getTest() {
        return "testSuccess" ;
    }
}
