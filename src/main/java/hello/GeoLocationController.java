package hello;

import java.util.Collection;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hello.Application.GeoLocationDetails;
import hello.Application.LocationForGrafana;

@RestController
public class GeoLocationController {

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/geolocation", method = RequestMethod.GET)
    public Collection<LocationForGrafana> geolocation() {
        System.out.println(GeoLocationDetails.get().getLocations());
        return GeoLocationDetails.get().getLocations();
    }
    
    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/influx/query", method = RequestMethod.GET)
    public void influxQuery() {
        
    }
}
