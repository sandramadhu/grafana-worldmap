package hello;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SpringBootApplication
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public static void main(String args[]) throws IOException {
        GeoLocationDetails.get().process(loadIps());
        SpringApplication.run(Application.class);
    }

    private static List<String> loadIps() throws IOException {
        Resource res = new ClassPathResource("ips.txt");
        BufferedInputStream is = new BufferedInputStream(res.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            return br.lines().collect(Collectors.toList());
        } finally {
            br.close();
        }
    }

    public static class GeoLocationDetails {
        private static final String GEOLOCATION_API_URL = "https://madhu-geolocation.herokuapp.com/json/";
        private static final GeoLocationDetails INSTANCE = new GeoLocationDetails();

        private final Collection<LocationForGrafana> locations = new HashSet<>();
        private final Collection<LocationInfo> locationInfos = new HashSet<>();

        private GeoLocationDetails() {
        }

        public static GeoLocationDetails get() {
            return INSTANCE;
        }
        
        public Collection<LocationForGrafana> getLocations() {
            return this.locations;
        }

        public Collection<LocationForGrafana> process(List<String> ips) {
            if (ips != null) {
                RestTemplate restTemplate = new RestTemplate();
                ips.stream().forEach(ip -> {
                    LocationInfo locationInfo = restTemplate.getForObject(GEOLOCATION_API_URL + ip, LocationInfo.class);
                    locationInfos.add(locationInfo);
                    
//                    LocationForGrafana location = new LocationForGrafana();
//                    locations.add(location);
//
//                    location.setIp(locationInfo.getIp());
//                    location.setKey(locationInfo.getCountry_code());
//                    location.setName(locationInfo.getCountry_name());
//                    location.setLatitude(locationInfo.getLatitude());
//                    location.setLongitude(locationInfo.getLongitude());
                });
            }
            
            InfluxDBPersister persister = new InfluxDBPersister();
            persister.persist(locationInfos);
            
            return locations;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationInfo {
        private String ip;
        private String country_code;
        private String country_name;
        private String region_code;
        private String region_name;
        private String city;
        private Double latitude;
        private Double longitude;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getCountry_code() {
            return country_code;
        }

        public void setCountry_code(String country_code) {
            this.country_code = country_code;
        }

        public String getCountry_name() {
            return country_name;
        }

        public void setCountry_name(String country_name) {
            this.country_name = country_name;
        }

        public String getRegion_code() {
            return region_code;
        }

        public void setRegion_code(String region_code) {
            this.region_code = region_code;
        }

        public String getRegion_name() {
            return region_name;
        }

        public void setRegion_name(String region_name) {
            this.region_name = region_name;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ip == null) ? 0 : ip.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LocationInfo other = (LocationInfo) obj;
            if (ip == null) {
                if (other.ip != null)
                    return false;
            } else if (!ip.equals(other.ip))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "LocationInfo [ip=" + ip + ", country_code=" + country_code + ", country_name=" + country_name
                    + ", region_code=" + region_code + ", region_name=" + region_name + ", city=" + city + ", latitude="
                    + latitude + ", longitude=" + longitude + "]";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationForGrafana {
        private String ip;
        private String key;
        private String name;
        private Double latitude;
        private Double longitude;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ip == null) ? 0 : ip.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LocationForGrafana other = (LocationForGrafana) obj;
            if (ip == null) {
                if (other.ip != null)
                    return false;
            } else if (!ip.equals(other.ip))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "LocationForGrafana [ip=" + ip + ", key=" + key + ", name=" + name + ", latitude=" + latitude
                    + ", longitude=" + longitude + "]";
        }
    }
}