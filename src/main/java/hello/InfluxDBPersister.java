package hello;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ch.hsr.geohash.GeoHash;
import hello.Application.LocationInfo;

@Service
public class InfluxDBPersister {
    private static final String DB_NAME = "scrtmetrics";
    private static final String INFLUX_DB_URL = "http://localhost:8086";
    private InfluxDB influxDB;
    
    public InfluxDBPersister() {
        influxDB = InfluxDBFactory.connect(INFLUX_DB_URL, "root", "root");
        influxDB.createDatabase(DB_NAME);
    }

    public void persist(Collection<LocationInfo> locationInfos) {
        System.out.println("Writing to Influxdb...total size: " + locationInfos.size());
        BatchPoints batchPoints = BatchPoints
                        .database(DB_NAME)
                        .tag("async", "true")
                        .retentionPolicy("autogen")
                        .consistency(ConsistencyLevel.ALL)
                        .build();
        
        persistInternal(batchPoints, locationInfos);
    }
    
    private void persistInternal(BatchPoints batchPoints, Collection<LocationInfo> locationInfos) {
        try {
            Iterator<LocationInfo> iter = locationInfos.iterator();
            while(iter.hasNext()) {
                LocationInfo location = iter.next();
                
                String geoHash = GeoHash.withCharacterPrecision(location.getLatitude(), location.getLongitude(), 12).toBase32();
                Builder builder = Point.measurement("chats.geolocation.count")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .addField("value", 1L)
                        .addField("geohash", geoHash)
                        .addField("latitude", location.getLatitude())
                        .addField("longitude", location.getLongitude())
                        .tag("latitude", String.valueOf(location.getLatitude()))
                        .tag("longitude", String.valueOf(location.getLongitude()))
                        .tag("geohash", geoHash)
                        .tag("ip", location.getIp());
                if(!StringUtils.isEmpty(location.getCity())) {
                    builder.addField("city", location.getCity());
                    builder.tag("city", location.getCity());
                }
                if(!StringUtils.isEmpty(location.getCountry_code())) {
                    builder.addField("country", location.getCountry_code());
                    builder.tag("country", location.getCountry_code());
                }
                if(!StringUtils.isEmpty(location.getRegion_code())) {
                    builder.addField("state", location.getRegion_code());
                    builder.tag("state", location.getRegion_code());
                }
                
                batchPoints.point(builder.build());
            }
        } finally {
            influxDB.write(batchPoints);
            influxDB.close();
        }
    }
}
