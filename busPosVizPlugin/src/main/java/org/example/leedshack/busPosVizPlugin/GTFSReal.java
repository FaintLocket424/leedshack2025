package org.example.leedshack.busPosVizPlugin;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class GTFSReal {
    public static ArrayList<Bus> regionBuses(GlobalLocation southWest, GlobalLocation northEast) throws IOException {

        URL url = new URL("https://data.bus-data.dft.gov.uk/api/v1/gtfsrtdatafeed/" +
                "?boundingBox=" +
                southWest.longitude() + "," + southWest.latitude() + "," +
                northEast.longitude() + "," + northEast.latitude() +
                "&api_key=" +
                System.getenv("BUS_API_KEY")
        );
        FeedMessage feed = FeedMessage.parseFrom(url.openStream());

        ArrayList<Bus> buses = new ArrayList<Bus>();

        for (FeedEntity entity : feed.getEntityList()) {
            GlobalLocation tempLocation = new GlobalLocation(entity.getVehicle().getPosition().getLongitude(), entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getPosition().getBearing());
            Bus tempBus = new Bus(tempLocation, entity.getVehicle().getVehicle().getId());
            buses.add(tempBus);
        }

        return buses;
    }
}
