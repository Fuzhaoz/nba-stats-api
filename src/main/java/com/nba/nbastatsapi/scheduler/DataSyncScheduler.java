package com.nba.nbastatsapi.scheduler;
import com.nba.nbastatsapi.service.BackfillService;
import com.nba.nbastatsapi.service.GameService;
import com.nba.nbastatsapi.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.nba.nbastatsapi.service.BoxScoreService;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSyncScheduler {

    private final TeamService teamService;
    private final GameService gameService;
    private final BackfillService backfillService;
    private final BoxScoreService boxScoreService;


    @Scheduled(cron = "0 0 2 * * *")
    public void syncTeams() {
        log.info("Starting team sync...");
        teamService.syncTeams();
        log.info("Team sync completed!");
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void syncTodayGames() {

        log.info("Starting NBA games sync...");

        //  Backfill missing games from previous dates
        backfillService.backfillGames();

        // update today games
        gameService.syncTodayGames();

        log.info("NBA games sync completed!");
    }
    @Scheduled(cron = "0 5,35 * * * *", zone = "America/New_York")
    public void scheduledStandingsSync() {

        int season = getCurrentNbaSeason();

        teamService.syncStandings(season);
    }

    private int getCurrentNbaSeason() {

        LocalDate today = LocalDate.now(
                ZoneId.of("America/New_York")
        );

        int year = today.getYear();
        int month = today.getMonthValue();

        return month >= 10 ? year : year - 1;
    }

    // Sync box scores for newly completed games
    @Scheduled(cron = "0 10 * * * *", zone = "America/New_York")
    public void scheduledBoxScoreSync() {
        log.info("Starting scheduled box score sync...");
        backfillService.backfillBoxScores();
    }

}
