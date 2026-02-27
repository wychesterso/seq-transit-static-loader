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
public class StopLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(StopLoader.class);

    public StopLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void load(Path gtfsDir) throws Exception {

        Path stopsFile = gtfsDir.resolve("stops.txt");
        if (!Files.exists(stopsFile)) {
            throw new IllegalStateException("stops.txt not found in " + gtfsDir);
        }

        long start = System.currentTimeMillis();
        log.info("Starting StopLoader...");

        try (Connection conn = dataSource.getConnection()) {

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            // drop indexes to speed up bulk insert
            try (Statement st = conn.createStatement()) {
                log.info("Dropping indexes...");
                st.execute("DROP INDEX IF EXISTS idx_stops_lat_lon");
            }

            // clear table
            log.info("Truncating stops...");
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE stops");
            }

            // copy raw csv to staging
            log.info("Starting COPY stops...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(stopsFile)) {

                long rows = copy.copyIn("""
                            COPY stops (
                                stop_id,
                                stop_code,
                                stop_name,
                                stop_desc,
                                stop_lat,
                                stop_lon,
                                zone_id,
                                stop_url,
                                location_type,
                                parent_station,
                                platform_code
                            )
                            FROM STDIN WITH (FORMAT csv, HEADER true)
                        """, reader);

                log.info("COPY stops finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            // recreate indexes
            try (Statement st = conn.createStatement()) {
                log.info("Recreating indexes...");
                st.execute("""
                    CREATE INDEX IF NOT EXISTS idx_stops_lat_lon
                    ON stops (stop_lat, stop_lon);
                """);
                log.info("Indexes recreated");
            }

            log.info("StopLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}
