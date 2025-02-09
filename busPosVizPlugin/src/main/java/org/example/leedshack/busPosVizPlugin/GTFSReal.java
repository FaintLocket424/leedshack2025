package org.example.leedshack.busPosVizPlugin;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GTFSReal {

    public static List<Bus> regionBuses(BoundingBox bb) {
        FeedMessage feed;

        try {
            URL url = new URL("https://data.bus-data.dft.gov.uk/api/v1/gtfsrtdatafeed/" +
                "?boundingBox=" +
                bb.southWest().longitude() + "," + bb.southWest().latitude() + "," +
                bb.northEast().longitude() + "," + bb.northEast().latitude() +
                "&api_key=" +
                System.getenv("BUS_API_KEY")
            );

            feed = FeedMessage.parseFrom(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Bus> buses = new ArrayList<>();

        for (FeedEntity entity : feed.getEntityList()) {
            Position position = entity.getVehicle().getPosition();

            GlobalLocation tempLocation = new GlobalLocation(
                position.getLongitude(),
                position.getLatitude(),
                position.getBearing()
            );

            buses.add(new Bus(tempLocation, entity.getVehicle().getVehicle().getId()));
        }

        return buses;
    }
}
