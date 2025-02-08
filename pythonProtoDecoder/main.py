import os

import gtfs_realtime_pb2
import urllib.request  # For fetching from a URL

# Or use a file:
# with open("path/to/your/gtfs_realtime_data.bin", "rb") as f:
#     feed_data = f.read()
# 1. Read the binary data (replace with your actual URL or file path)
url = ("https://data.bus-data.dft.gov.uk/api/v1/gtfsrtdatafeed/"
       "?boundingBox=-2.93,53.374,-3.085,53.453"
       # "&routeId=45,26"
       # "&startTimeAfter=1609518528"
       # "&startTimeBefore=1609518528#"
       f"&api_key={os.environ['BUS_API_KEY']}"
       )  # Replace with your GTFS-RT feed URL
try:
    response = urllib.request.urlopen(url)
    feed_data = response.read()
except urllib.error.URLError as e:
    print(f"Error fetching data: {e}")
    exit()  # Or handle the error appropriately

# 2. Create a GTFS-RT feed message
feed = gtfs_realtime_pb2.FeedMessage()

# 3. Parse the binary data
try:
    feed.ParseFromString(feed_data)
except Exception as e:
    print(f"Error parsing data: {e}")
    exit()  # Or handle the error appropriately

print(feed)
# 4. Access the data
# for entity in feed.entity:
#     if entity.HasField('trip_update'):
#         trip_update = entity.trip_update
#         print(f"Trip ID: {trip_update.trip_id}")
#
#         for stop_time_update in trip_update.stop_time_update:
#             print(f"  Stop Sequence: {stop_time_update.stop_sequence}")
#
#             if stop_time_update.HasField('arrival'):
#                 arrival_time = stop_time_update.arrival.time
#                 print(f"    Arrival Time: {arrival_time}")  # This is a Unix timestamp
#
#             if stop_time_update.HasField('departure'):
#                 departure_time = stop_time_update.departure.time
#                 print(f"    Departure Time: {departure_time}")  # This is a Unix timestamp
#
#             # ... access other fields (e.g., delay) ...
#
#     elif entity.HasField('vehicle_position'):
#         vehicle_position = entity.vehicle_position
#         print(f"Vehicle ID: {vehicle_position.vehicle.id}")
#         if vehicle_position.HasField('position'):
#             latitude = vehicle_position.position.latitude
#             longitude = vehicle_position.position.longitude
#             print(f"  Latitude: {latitude}, Longitude: {longitude}")
#         # ... access other fields ...
#
#     elif entity.HasField('alert'):
#         alert = entity.alert
#         print("Alert active_period:")
#         for period in alert.active_period:
#             print(f"   Start: {period.start}, End: {period.end}")
#         # ... access other fields ...

# Now you can work with the parsed data!
