package com.wychesterso.transit.seq_transit_static_loader;

import com.wychesterso.transit.seq_transit_static_loader.importer.GtfsImportRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class StaticLoaderRunner {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(StaticLoaderRunner.class)
                .web(WebApplicationType.NONE) // disable web server
                .run(args)
                .getBean(GtfsImportRunner.class)
                .runImport();
    }
}
