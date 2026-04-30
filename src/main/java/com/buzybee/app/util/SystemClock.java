package com.buzybee.app.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


/**
 * Centralized time-keeping utility.
 * Allows the application to simulate "time travel" for testing and
 * demonstrations by overriding system-current dates and times.
 */
public class SystemClock {

    // ==========================================
    // Testing Purposes
    // ==========================================
    private static boolean isSimulationMode = false;
    private static LocalDate simulatedDate = null;
    private static LocalTime simulatedTime = null;

    // ==========================================
    // 1. CORE TIME METHODS
    // ==========================================

    // Use SystemClock.today() instead of LocalDate.now() everywhere in your app!
    public static LocalDate today() {
        if (isSimulationMode && simulatedDate != null) {
            return simulatedDate;
        }
        return LocalDate.now();
    }

    // Use SystemClock.now() instead of LocalTime.now()
    public static LocalTime now() {
        if (isSimulationMode && simulatedTime != null) {
            return simulatedTime;
        }
        return LocalTime.now();
    }

    public static LocalDateTime currentDateTime() {
        return LocalDateTime.of(today(), now());
    }
}