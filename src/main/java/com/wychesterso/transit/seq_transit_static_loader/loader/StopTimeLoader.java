package com.wychesterso.transit.seq_transit_static_loader.loader;

import com.wychesterso.transit.seq_transit_static_loader.time.ServiceClock;
import com.wychesterso.transit.seq_transit_static_loader.time.ServiceTimeHelper;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

@Component
public class StopTimeLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(StopTimeLoader.class);

    public StopTimeLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void load(Path gtfsDir) throws Exception {

        Path stopTimesFile = gtfsDir.resolve("stop_times.txt");
        if (!Files.exists(stopTimesFile)) {
            throw new IllegalStateException("stop_times.txt not found in " + gtfsDir);
        }

        // filter by current service date
        ServiceClock clock = ServiceTimeHelper.now();
        LocalDate serviceDate = clock.serviceDate();
        String sqlDate = serviceDate.toString();

        long start = System.currentTimeMillis();
        log.info("Starting StopTimeLoader using {}", stopTimesFile);

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            // drop indexes to speed up bulk insert
            try (Statement st = conn.createStatement()) {
                log.info("Dropping indexes...");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_stop_id");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_trip_id");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_stop_trip");
                st.execute("DROP INDEX IF EXISTS idx_stop_times_stop_arrival");
            }

            // create staging table
            log.info("Creating temp stop_times_raw...");
            try (Statement st = conn.createStatement()) {
                st.execute("""
                    DROP TABLE IF EXISTS stop_times_raw;

                    CREATE TEMP TABLE stop_times_raw (
                        trip_id TEXT NOT NULL,
                        arrival_time TEXT,
                        departure_time TEXT,
                        stop_id TEXT,
                        stop_sequence INTEGER NOT NULL,
                        pickup_type INTEGER,
                        dropoff_type INTEGER
                    ) ON COMMIT DROP
                """);
            }

            // copy raw csv to staging
            log.info("Starting COPY stop_times_raw...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(stopTimesFile)) {

                long rows = copy.copyIn("""
                    COPY stop_times_raw (
                        trip_id,
                        arrival_time,
                        departure_time,
                        stop_id,
                        stop_sequence,
                        pickup_type,
                        dropoff_type
                    )
                    FROM STDIN WITH (FORMAT csv, HEADER true)
                """, reader);

                log.info("COPY stop_times_raw finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            // build active services
            log.info("Building active_services temp table...");

            try (Statement st = conn.createStatement()) {

                st.execute("""
                    CREATE TEMP TABLE active_services(
                        service_id TEXT PRIMARY KEY
                    ) ON COMMIT DROP
                """);

                // calendar rules
                st.execute("""
                    INSERT INTO active_services(service_id)
                    SELECT service_id
                    FROM calendar
                    WHERE
                        start_date <= DATE '%s'
                        AND end_date >= DATE '%s'
                        AND CASE EXTRACT(DOW FROM DATE '%s')
                            WHEN 0 THEN sunday
                            WHEN 1 THEN monday
                            WHEN 2 THEN tuesday
                            WHEN 3 THEN wednesday
                            WHEN 4 THEN thursday
                            WHEN 5 THEN friday
                            WHEN 6 THEN saturday
                        END
                """.formatted(sqlDate, sqlDate, sqlDate));

                // added services
                st.execute("""
                    INSERT INTO active_services(service_id)
                    SELECT service_id
                    FROM calendar_dates
                    WHERE date = DATE '%s'
                    AND exception_type = 1
                """.formatted(sqlDate));

                // removed services
                st.execute("""
                    DELETE FROM active_services
                    WHERE service_id IN (
                        SELECT service_id
                        FROM calendar_dates
                        WHERE date = DATE '%s'
                        AND exception_type = 2
                    )
                """.formatted(sqlDate));
            }

            // transform staging to actual
            log.info("Starting transform + insert into stop_times...");
            long insertStart = System.currentTimeMillis();

            try (Statement st = conn.createStatement()) {

                st.execute("""
                    TRUNCATE stop_times;

                    INSERT INTO stop_times (
                        trip_id,
                        arrival_time,
                        departure_time,
                        stop_id,
                        stop_sequence,
                        pickup_type,
                        dropoff_type
                    )
                    SELECT
                        str.trip_id,

                        split_part(arrival_time, ':', 1)::int * 3600
                            + split_part(arrival_time, ':', 2)::int * 60
                            + split_part(arrival_time, ':', 3)::int,

                        split_part(departure_time, ':', 1)::int * 3600
                            + split_part(departure_time, ':', 2)::int * 60
                            + split_part(departure_time, ':', 3)::int,

                        str.stop_id,
                        str.stop_sequence,
                        str.pickup_type,
                        str.dropoff_type

                    FROM stop_times_raw str
                    JOIN trips t USING (trip_id)
                    JOIN active_services s USING (service_id);
                """);
            }

            log.info("Insert finished in {} ms", System.currentTimeMillis() - insertStart);

            // recreate indexes
            try (Statement st = conn.createStatement()) {
                log.info("Recreating indexes...");
                st.execute("""
                    CREATE INDEX IF NOT EXISTS idx_stop_times_stop_id
                    ON stop_times (stop_id);
                    CREATE INDEX IF NOT EXISTS idx_stop_times_trip_id
                    ON stop_times (trip_id);
                    CREATE INDEX IF NOT EXISTS idx_stop_times_stop_trip
                    ON stop_times (stop_id, trip_id);
                    CREATE INDEX IF NOT EXISTS idx_stop_times_stop_arrival
                    ON stop_times (stop_id, arrival_time);
                """);
            }

            log.info("Committing changes...");
            conn.commit();

            log.info("StopTimeLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}