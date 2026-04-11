package com.nba.nbastatsapi.scheduler;

import com.nba.nbastatsapi.service.GameService;
import com.nba.nbastatsapi.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSyncScheduler {

    private final TeamService teamService;
    private final GameService gameService;

    @Scheduled(cron = "0 0 2 * * *")
    public void syncTeams() {
        log.info("Starting team sync...");
        teamService.syncTeams();
        log.info("Team sync completed!");
    }

    @Scheduled(cron = "1 * * * * *")
    public void syncTodayGames() {
        log.info("Starting today games sync...");
        gameService.syncTodayGames();
        log.info("Today games sync completed!");
    }

}
