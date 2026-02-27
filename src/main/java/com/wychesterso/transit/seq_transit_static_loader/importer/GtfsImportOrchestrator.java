package com.wychesterso.transit.seq_transit_static_loader.importer;

import com.wychesterso.transit.seq_transit_static_loader.loader.CalendarLoader;
import com.wychesterso.transit.seq_transit_static_loader.loader.CalendarDateLoader;
import com.wychesterso.transit.seq_transit_static_loader.loader.RouteLoader;
import com.wychesterso.transit.seq_transit_static_loader.loader.ShapeLoader;
import com.wychesterso.transit.seq_transit_static_loader.loader.StopTimeLoader;
import com.wychesterso.transit.seq_transit_static_loader.loader.StopLoader;
import com.wychesterso.transit.seq_transit_static_loader.loader.TripLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

@Component
public class GtfsImportOrchestrator {

    private final RouteLoader routeLoader;
    private final StopLoader stopLoader;
    private final CalendarLoader calendarLoader;
    private final CalendarDateLoader calendarDateLoader;
    private final TripLoader tripLoader;
    private final ShapeLoader shapeLoader;
    private final StopTimeLoader stopTimeLoader;

    public GtfsImportOrchestrator(
            RouteLoader routeLoader,
            StopLoader stopLoader,
            CalendarLoader calendarLoader,
            CalendarDateLoader calendarDateLoader,
            TripLoader tripLoader,
            ShapeLoader shapeLoader,
            StopTimeLoader stopTimeLoader) {
        this.routeLoader = routeLoader;
        this.stopLoader = stopLoader;
        this.calendarLoader = calendarLoader;
        this.calendarDateLoader = calendarDateLoader;
        this.tripLoader = tripLoader;
        this.shapeLoader = shapeLoader;
        this.stopTimeLoader = stopTimeLoader;
    }

    @Transactional
    public void importGtfs(Path gtfsDir) throws Exception {
        try {
            routeLoader.load(gtfsDir);
            stopLoader.load(gtfsDir);
            calendarLoader.load(gtfsDir);
            calendarDateLoader.load(gtfsDir);
            tripLoader.load(gtfsDir);
            shapeLoader.load(gtfsDir);
            stopTimeLoader.load(gtfsDir);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}