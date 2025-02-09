package org.example.leedshack.busPosVizPlugin;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public class Placeholder {

    public static final double DEFAULT_SPAN = 0.05;

    public static BoundingBox getBoundingBox(GlobalLocation centre, double span) {
        var northEast = new GlobalLocation(centre.longitude()+span, centre.latitude()+span, 0);
        var southWest = new GlobalLocation(centre.longitude()-span, centre.latitude()-span, 0);

        return new BoundingBox(northEast, southWest);
    }

    public static List<BusStop> stopsWithinRegion(BoundingBox bb, String csvFilename) {

        List<BusStop> stops = new ArrayList<>();

        int long_index = 29;
        int lat_index = 30;
        int common_name_index = 4;
        int bus_stop_type_index = 32;

        Path path = Paths.get(csvFilename);;
//        try {
//            BusPosVizPlugin.instance.getLogger().info("Loading file: " + csvFilename);
////            path = Paths.get(
////                ClassLoader.getSystemResource(csvFilename).toURI()
////            );
//
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }

        try (Reader reader = Files.newBufferedReader(path)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                csvReader.readNext();
                String[] line;
                while ((line = csvReader.readNext()) != null) {
//                    Logger logger = BusPosVizPlugin.instance.getLogger();
//                    logger.info(String.format("Processing line: \"%s\"",
//                        Arrays.toString(line)));
//
//                    for (int i = 0; i < line.length; i++) {
//                        String s = line[i];
//                        logger.info(String.format("Line[%d]: %s", i, s));
//                    }
//
//                    return null;

                    String str_longitude = line[long_index];
                    String str_latitude = line[lat_index];

                    if (Objects.equals(str_longitude, "") || Objects.equals(str_latitude, "")) continue;

                    GlobalLocation loc;
                    try {
                        loc = new GlobalLocation(Double.parseDouble(line[29]),
                            Double.parseDouble(line[30]), 0);
                    } catch (NumberFormatException e) {
                        BusPosVizPlugin.instance.getLogger().info(String.format("Errored parsing %s or %s because it's not a double", line[29], line[30]));
                        continue;
                    }

                    boolean isBusStop = !Objects.equals(line[bus_stop_type_index], "");

                    if (!isBusStop) continue;

                    String name = line[common_name_index];

                    stops.add(new BusStop(loc, name));

//                    boolean long_within = loc.longitude() <= bb.northEast().longitude() && loc.longitude() >= bb.southWest().longitude();
//                    boolean lat_within = loc.latitude() <= bb.northEast().latitude() && loc.latitude() >= bb.southWest().latitude();
                }
            } catch (IOException | CsvValidationException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stops;
    }
}
