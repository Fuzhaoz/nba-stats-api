package com.nba.nbastatsapi.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class SeasonService {

    private static final ZoneId NBA_ZONE =
            ZoneId.of("America/New_York");

    public int getCurrentSeason() {

        LocalDate today = LocalDate.now(NBA_ZONE);
        int year = today.getYear();

        if (today.getMonthValue() >= 10) {
            return year;
        }

        return year - 1;
    }

    // Get today's date in the NBA timezone
    public LocalDate getNbaToday() {
        return LocalDate.now(NBA_ZONE);
    }

}