package com.wychesterso.transit.seq_transit_static_loader.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class GtfsZipExtractor {

    private static final Logger log = LoggerFactory.getLogger(GtfsZipDownloader.class);

    public Path extract(Path zipPath, Path extractDir) throws IOException {

        log.info("Extracting GTFS ZIP: {}", zipPath);
        int fileCount = 0;
        long start = System.currentTimeMillis();

        Files.createDirectories(extractDir);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path out = extractDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                }
                fileCount++;
            }
        }

        log.info("Extracted {} GTFS files in {} ms",
                fileCount,
                System.currentTimeMillis() - start);

        return extractDir;
    }
}