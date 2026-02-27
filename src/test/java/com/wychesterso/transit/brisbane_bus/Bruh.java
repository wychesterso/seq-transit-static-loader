package com.wychesterso.transit.brisbane_bus;

public class Bruh {
    public static void main(String[] args) {
        printCalendar();
    }

    private static void printStops() {
        try {
//            for (Stop stop : StopLoader.loadStops()) {
//                System.out.println(stop);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printRoutes() {
        try {
//            for (Route route : RouteLoader.loadRoutes()) {
//                System.out.println(route);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printTrips() {
        try {
//            for (Trip trip : TripLoader.loadTrips()) {
//                System.out.println(trip);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printStopTimes() {
        try {
//            for (StopTime stopTime : StopTimeLoader.loadStopTimes()) {
//                System.out.println(stopTime);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printCalendar() {
        try {
//            for (Calendar calendar : CalendarLoader.loadCalendar()) {
//                System.out.println(calendar);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
