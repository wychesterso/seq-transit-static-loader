package com.wychesterso.transit.seq_transit_static_loader.time;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ServiceTimeHelper {

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    private static final LocalTime DAY_CUTOFF = LocalTime.of(3, 0); // 3am

    public static ServiceClock now() {
        ZonedDateTime now = ZonedDateTime.now(BRISBANE);
        int clockSeconds = now.toLocalTime().toSecondOfDay();

        LocalDate serviceDate;
        int nowSeconds;

        if (now.toLocalTime().isBefore(DAY_CUTOFF)) {
            serviceDate = now.toLocalDate().minusDays(1);
            nowSeconds = clockSeconds + 86400;
        } else {
            serviceDate = now.toLocalDate();
            nowSeconds = clockSeconds;
        }

        return new ServiceClock(serviceDate, nowSeconds);
    }
}
