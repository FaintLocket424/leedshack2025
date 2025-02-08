import csv

# Used for leedsStops.csv
# topright = [53.836971, -1.428635]
# bottomleft = [53.766816, -1.649048]

topright = [53.838996, -1.472580]
bottomleft = [53.756262, -1.620895]
new_file_name = "leedsStops.csv"


with open("Stops.csv", 'r', newline='', encoding='utf-8') as infile, \
    open(new_file_name, 'w', newline='', encoding='utf-8') as outfile:

    reader = csv.reader(infile)
    writer = csv.writer(outfile)

    header = next(reader)
    # writer.writerow(header)

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
            if longitude <= topright[1] and longitude >= bottomleft[1] and latitude <= topright[0] and latitude >= bottomleft[0]:
                if row[header.index("BusStopType")] != '':
                    new_row = []
                    for index in columns:
                        new_row.append(row[index])
                    writer.writerow(new_row)
                    # writer.writerow(row)
                # else:
                #     print(row[header.index("CommonName")])
        except:
            pass
