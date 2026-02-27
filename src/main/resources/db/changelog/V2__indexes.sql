--liquibase formatted sql

-- changeset wychesterso:2

-- routes
CREATE INDEX idx_routes_short_name
ON routes (route_short_name text_pattern_ops);

-- stops
CREATE INDEX idx_stops_lat_lon
ON stops (stop_lat, stop_lon);

-- trips
CREATE INDEX idx_trips_trip_id
ON trips (trip_id);

CREATE INDEX idx_trips_route_id
ON trips (route_id);

CREATE INDEX idx_trips_shape_id
ON trips (shape_id);

CREATE INDEX idx_trips_trip_route
ON trips (trip_id, route_id);

-- shapes
CREATE INDEX idx_shapes_shape_id
ON shapes(shape_id);

-- stop_times
CREATE INDEX idx_stop_times_stop_id
ON stop_times (stop_id);

CREATE INDEX idx_stop_times_trip_id
ON stop_times (trip_id);

CREATE INDEX idx_stop_times_stop_trip
ON stop_times (stop_id, trip_id);

CREATE INDEX idx_stop_times_stop_arrival
ON stop_times (stop_id, arrival_time);