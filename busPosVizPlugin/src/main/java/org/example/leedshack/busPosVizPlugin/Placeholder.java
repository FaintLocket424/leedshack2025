package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Placeholder {

    public static final double DEFAULT_SPAN = 0.05;

    public static BoundingBox getBoundingBox(GlobalLocation centre, double span) {
        var northEast = new GlobalLocation(centre.longitude()+span, centre.latitude()+span, 0);
        var southWest = new GlobalLocation(centre.longitude()-span, centre.latitude()-span, 0);

        return new BoundingBox(northEast, southWest);
    }

    public static List<BusStop> stopsWithinRegion(GlobalLocation gloc, String csvFilename) {
        return stopsWithinRegion(gloc, csvFilename, DEFAULT_SPAN);
    }

    public static List<BusStop> stopsWithinRegion(GlobalLocation centre, String csvFilename, double span) {
        BoundingBox bb = getBoundingBox(centre, span);

        List<BusStop> stops = new ArrayList<>();

        int long_index = 29;
        int lat_index = 30;
        int common_name_index = 4;
        int bus_stop_type_index = 32;

        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    var loc = new GlobalLocation(Double.parseDouble(line[29]), Double.parseDouble(line[30]), 0);

                    boolean long_within = loc.longitude() <= bb.northEast().longitude() && loc.longitude() >= bb.southWest().longitude();
                    boolean lat_within = loc.longitude() <= bb.northEast().longitude() && loc.longitude() >= bb.southWest().longitude();
                }
            } catch (IOException | CsvValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
