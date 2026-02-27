package com.wychesterso.transit.seq_transit_static_loader.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class GtfsZipDownloader {

    private static final Logger log = LoggerFactory.getLogger(GtfsZipDownloader.class);

    public Path download(String url, Path targetDir) throws IOException, InterruptedException {
        log.info("Downloading GTFS ZIP from {}", url);
        long start = System.currentTimeMillis();

        Files.createDirectories(targetDir);

        Path zipPath = targetDir.resolve("gtfs.zip");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();

        client.send(
                request,
                HttpResponse.BodyHandlers.ofFile(zipPath)
        );

        long size = Files.size(zipPath);

        log.info("Downloaded GTFS ZIP ({} MB) in {} ms",
                size / (1024 * 1024),
                System.currentTimeMillis() - start);

        return zipPath;
    }
}