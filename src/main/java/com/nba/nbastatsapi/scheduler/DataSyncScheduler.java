package com.nba.nbastatsapi.scheduler;
import com.nba.nbastatsapi.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSyncScheduler {

    private final TeamService teamService;
    private final GameService gameService;
    private final BackfillService backfillService;
    private final BoxScoreService boxScoreService;
    private final TeamSeasonStatsService teamSeasonStatsService;
    private final PredictionService predictionService;
    private final OddsService oddsService;
    private final PlayerLeaderSyncService playerLeaderSyncService;


    @Scheduled(cron = "0 0 2 * * *")
    public void syncTeams() {
        log.info("Starting team sync...");
        teamService.syncTeams();
        log.info("Team sync completed!");
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void syncGameMaintenance() {

        log.info("Starting NBA games sync...");

        // Backfill missing games from previous dates
        backfillService.backfillGames();


        // 3. Sync odds after games exist
        try {
            log.info("Starting NBA odds sync...");

            int oddsCount = oddsService.syncOdds();

            log.info("NBA odds sync completed. Synced {} records.", oddsCount);

        } catch (Exception e) {
            log.error("NBA odds sync failed", e);
        }

        // Settle predictions after scores are updated
        int settledCount = predictionService.settlePredictions();

        log.info("Settled {} predictions.", settledCount);

        log.info("NBA games sync completed!");
    }
    @Scheduled(fixedDelay = 60000)
    public void syncGameScores() {

        log.info("Starting game score sync...");

        try {
            gameService.syncTodayGames();

            log.info("Game score sync completed.");

        } catch (Exception e) {
            log.error("Game score sync failed", e);
        }
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
    @Scheduled(cron = "0 */30 * * * *", zone = "America/New_York")
    public void scheduledBoxScoreSync() {
        log.info("Starting scheduled box score sync...");
        backfillService.backfillBoxScores();
    }


    @Scheduled(cron = "0 0 6 * * *", zone = "America/New_York")
    public void scheduledTeamSeasonStatsSync() {

        log.info("Starting team season stats sync...");

        try {
            int count = teamSeasonStatsService.syncTeamSeasonStats();

            log.info("Team season stats sync completed. Synced {} records.", count);

        } catch (Exception e) {
            log.error("Team season stats sync failed", e);
        }
    }
    @Scheduled(cron = "0 0 6 * * *", zone = "America/New_York")
    public void scheduledPlayerLeadersSync() {

        log.info("Starting player leaders sync...");

        List<String> statTypes =
                List.of("pts", "reb", "ast", "blk", "stl");

        for (String statType : statTypes) {
            try {
                int count = playerLeaderSyncService.syncPlayerLeaders(statType);

                log.info(
                        "Player leaders sync completed for {}. Synced {} records.",
                        statType,
                        count
                );

            } catch (Exception e) {
                log.error(
                        "Player leaders sync failed for {}",
                        statType,
                        e
                );
            }
        }

        log.info("Player leaders scheduled sync finished.");
    }

}
