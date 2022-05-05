package de.fmi.searouter.rest;

import de.fmi.searouter.domain.RoutingRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoutingController {

    @GetMapping("/route")
    public String getRoute(@RequestBody RoutingRequest routingRequest) {
        return "success, lat_end_point: " + routingRequest.getEndPoint().getLatitude() ;
    }

    @GetMapping("/test")
    public String getTest() {
        return "testSuccess" ;
    }
}
