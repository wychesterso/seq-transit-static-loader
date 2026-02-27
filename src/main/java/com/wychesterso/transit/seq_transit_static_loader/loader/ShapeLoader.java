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
public class ShapeLoader {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(ShapeLoader.class);

    public ShapeLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void load(Path gtfsDir) throws Exception {

        Path shapesFile = gtfsDir.resolve("shapes.txt");
        if (!Files.exists(shapesFile)) {
            throw new IllegalStateException("shapes.txt not found in " + gtfsDir);
        }

        long start = System.currentTimeMillis();
        log.info("Starting ShapeLoader...");

        try (Connection conn = dataSource.getConnection()) {

            try (Statement st = conn.createStatement()) {
                st.execute("SET synchronous_commit = OFF");
            }

            PGConnection pg = conn.unwrap(PGConnection.class);
            CopyManager copy = pg.getCopyAPI();

            // drop indexes to speed up bulk insert
            try (Statement st = conn.createStatement()) {
                log.info("Dropping indexes...");
                st.execute("DROP INDEX IF EXISTS idx_shapes_shape_id");
            }

            // clear table
            log.info("Truncating shapes...");
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE shapes");
            }

            // copy raw csv to staging
            log.info("Starting COPY shapes...");
            long copyStart = System.currentTimeMillis();

            try (Reader reader = Files.newBufferedReader(shapesFile)) {

                long rows = copy.copyIn("""
                            COPY shapes (
                                shape_id,
                                shape_pt_lat,
                                shape_pt_lon,
                                shape_pt_sequence
                            )
                            FROM STDIN WITH (FORMAT csv, HEADER true)
                        """, reader);

                log.info("COPY shapes finished: {} rows in {} ms",
                        rows, System.currentTimeMillis() - copyStart);
            }

            // recreate indexes
            try (Statement st = conn.createStatement()) {
                log.info("Recreating indexes...");
                st.execute("""
                    CREATE INDEX IF NOT EXISTS idx_shapes_shape_id
                    ON shapes(shape_id);
                """);
                log.info("Indexes recreated");
            }

            log.info("ShapeLoader finished in {} ms",
                    System.currentTimeMillis() - start);
        }
    }
}
