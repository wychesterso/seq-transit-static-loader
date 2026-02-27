package com.wychesterso.transit.seq_transit_static_loader.time;

import java.time.LocalDate;

public record ServiceClock(
        LocalDate serviceDate,
        int serviceSeconds
) {}
