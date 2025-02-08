import csv

def bound_data(centre, stopsCSVFile, span=0.05):
    NE = [round(centre[0]+span, 6), round(centre[1]+span, 6)]
    SW = [round(centre[0]-span ,6), round(centre[1]-span, 6)]

    with open("Stops.csv", 'r', newline='', encoding='utf-8') as infile, \
        open(stopsCSVFile, 'w', newline='', encoding='utf-8') as outfile:

        reader = csv.reader(infile)
        writer = csv.writer(outfile)

        header = next(reader)

        var1_index = header.index("Longitude")
        var2_index = header.index("Latitude")

        col_names = ["CommonName", "Latitude", "Longitude"]
        columns = []
        for col in col_names:
            index = header.index(col)
            columns.append(index)

        new_col_names = ["Name", "Latitude", "Longitude"]

        writer.writerow(new_col_names)

        for row in reader:
            try:
                latitude = float(row[var2_index])
                longitude = float(row[var1_index])
                if longitude <= NE[1] and longitude >= SW[1] and latitude <= NE[0] and latitude >= SW[0]:
                    if row[header.index("BusStopType")] != '':
                        new_row = []
                        for index in columns:
                            new_row.append(row[index])
                        writer.writerow(new_row)
            except:
                pass
    
    return NE, SW


bound_data([53.7996, -1.5471], "leedsStops.csv")
bound_data([53.383331, -1.466667], "sheffieldStops.csv")
bound_data([53.483959, -2.244644], "manchesterStops.csv")
