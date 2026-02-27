package com.wychesterso.transit.seq_transit_static_loader.loader;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class TripLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(TripLoader.class);

    public TripLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void load(Path gtfsDir) throws Exception {

        Path tripsFile = gtfsDir.resolve("trips.txt");
        if (!Files.exists(tripsFile)) {
            throw new IllegalStateException("trips.txt not found in " + gtfsDir);
        }

        long start = System.currentTimeMillis();
        log.info("Starting TripLoader...");

        try (Connection conn = dataSource.getConnection()) {

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            // drop indexes to speed up bulk insert
            try (Statement st = conn.createStatement()) {
                log.info("Dropping indexes...");
                st.execute("DROP INDEX IF EXISTS idx_trips_trip_id");
                st.execute("DROP INDEX IF EXISTS idx_trips_route_id");
                st.execute("DROP INDEX IF EXISTS idx_trips_shape_id");
                st.execute("DROP INDEX IF EXISTS idx_trips_trip_route");
            }

            // clear table
            log.info("Truncating trips...");
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE trips");
            }

            // copy raw csv to staging
            log.info("Starting COPY trips...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(tripsFile)) {

                long rows = copy.copyIn("""
                    COPY trips (
                        route_id,
                        service_id,
                        trip_id,
                        trip_headsign,
                        direction_id,
                        block_id,
                        shape_id
                    )
                    FROM STDIN WITH (FORMAT csv, HEADER true)
                """, reader);

                log.info("COPY trips finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            // recreate indexes
            try (Statement st = conn.createStatement()) {
                log.info("Recreating indexes...");
                st.execute("""
                    CREATE INDEX IF NOT EXISTS idx_trips_trip_id
                    ON trips (trip_id);
                    CREATE INDEX IF NOT EXISTS idx_trips_route_id
                    ON trips (route_id);
                    CREATE INDEX IF NOT EXISTS idx_trips_shape_id
                    ON trips (shape_id);
                    CREATE INDEX IF NOT EXISTS idx_trips_trip_route
                    ON trips (trip_id, route_id);
                """);
                log.info("Indexes recreated");
            }

            log.info("TripLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}
