
package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.SyncStatus;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.PlayerGameStatsRepository;
import com.nba.nbastatsapi.repository.SyncStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackfillService {

    private static final String GAMES_SYNC_KEY = "NBA_GAMES";
    private static final String BOX_SCORE_SYNC_KEY = "NBA_BOX_SCORES";
    private static final ZoneId NBA_ZONE =
            ZoneId.of("America/New_York");

    private final SyncStatusRepository syncStatusRepository;
    private final GameService gameService;
    private final GameRepository gameRepository;
    private final BoxScoreService boxScoreService;
    private final PlayerGameStatsRepository statsRepository;
    private final SeasonService seasonService;


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Spring Boot started. Checking missing NBA games...");
        backfillGames();
    }


    public void backfillGames() {

        LocalDate today = LocalDate.now(NBA_ZONE);
        LocalDate lastCompletedDay = today.minusDays(1);
        LocalDate lastSyncedDate = syncStatusRepository
                .findById(GAMES_SYNC_KEY)
                .map(SyncStatus::getLastSyncedDate)
                .orElse(today.minusDays(7));

        LocalDate date = lastSyncedDate.plusDays(1);


        log.info("Starting backfill from {} to {}", date, lastCompletedDay);

        while (!date.isAfter(lastCompletedDay)) {

            gameService.syncGamesByDate(date);

            SyncStatus status = new SyncStatus();
            status.setSyncKey(GAMES_SYNC_KEY);
            status.setLastSyncedDate(date);
            syncStatusRepository.save(status);

            log.info("Backfill completed for {}", date);

            date = date.plusDays(1);
        }

        log.info("Backfill finished!");
    }
    // Backfill historical player box scores
    public void backfillBoxScores() {

        int season = seasonService.getCurrentSeason();
        int processedCount = 0;
        int successCount = 0;

        List<Game> games =
                gameRepository.findBySeasonOrderByDateAscIdAsc(season);

        log.info("Found {} games for season {}", games.size(), season);

        for (Game game : games) {

            // Skip unfinished games
            if (!"Final".equalsIgnoreCase(game.getStatus())) {
                continue;
            }

            // Skip already synced games
            if (Boolean.TRUE.equals(game.getBoxScoreSynced())) {
                continue;
            }

            processedCount++;

            try {
                Integer gameIdFromApi = game.getGameIdFromApi();

                if (gameIdFromApi == null) {
                    log.warn("Game {} has no API ID", game.getId());
                    continue;
                }

                // Sync box score and lineup data
                boxScoreService.syncBoxScore(gameIdFromApi);
                boxScoreService.syncLineups(gameIdFromApi);

                // Mark successful synchronization
                game.setBoxScoreSynced(true);
                gameRepository.save(game);

                successCount++;

                log.info("Successfully synced game {}", gameIdFromApi);

            } catch (Exception e) {
                log.error("Failed to sync game {}",
                        game.getGameIdFromApi(), e);
            }
        }

        log.info("Backfill completed. Processed: {}, Successful: {}",
                processedCount, successCount);
    }


}
