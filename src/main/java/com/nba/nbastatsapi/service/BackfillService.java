
package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.entity.SyncStatus;
import com.nba.nbastatsapi.repository.SyncStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackfillService {

    private static final String SYNC_KEY = "NBA_GAMES";
    private static final ZoneId NBA_ZONE =
            ZoneId.of("America/New_York");

    private final SyncStatusRepository syncStatusRepository;
    private final GameService gameService;


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Spring Boot started. Checking missing NBA games...");
        backfillGames();
    }


    public void backfillGames() {

        LocalDate today = LocalDate.now(NBA_ZONE);
        LocalDate lastCompletedDay = today.minusDays(1);
        LocalDate lastSyncedDate = syncStatusRepository
                .findById(SYNC_KEY)
                .map(SyncStatus::getLastSyncedDate)
                .orElse(today.minusDays(7));

        LocalDate date = lastSyncedDate.plusDays(1);


        log.info("Starting backfill from {} to {}", date, lastCompletedDay);

        while (!date.isAfter(lastCompletedDay)) {

            gameService.syncGamesByDate(date);

            SyncStatus status = new SyncStatus();
            status.setSyncKey(SYNC_KEY);
            status.setLastSyncedDate(date);
            syncStatusRepository.save(status);

            log.info("Backfill completed for {}", date);

            date = date.plusDays(1);
        }

        log.info("Backfill finished!");
    }
}
