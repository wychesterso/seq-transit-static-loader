package com.wychesterso.transit.seq_transit_static_loader.importer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class GtfsImportRunner {

    private final GtfsZipDownloader downloader;
    private final GtfsZipExtractor extractor;
    private final GtfsImportOrchestrator orchestrator;

    @Value("${gtfs.static.url}")
    private String gtfsUrl;

    @Value("${gtfs.workdir}")
    private String workDir;

    public GtfsImportRunner(
            GtfsZipDownloader downloader,
            GtfsZipExtractor extractor,
            GtfsImportOrchestrator orchestrator) {
        this.downloader = downloader;
        this.extractor = extractor;
        this.orchestrator = orchestrator;
    }

    public void runImport() throws Exception {
        Path base = Path.of(workDir);
        Path zip = downloader.download(gtfsUrl, base);
        Path extracted = extractor.extract(zip, base.resolve("extracted"));
        orchestrator.importGtfs(extracted);
    }
}