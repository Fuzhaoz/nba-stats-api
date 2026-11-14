package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.repository.GameOddsRepository;
import com.nba.nbastatsapi.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.GameOdds;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OddsService {

    private final RestClient restClient;
    private final GameRepository gameRepository;
    private final GameOddsRepository gameOddsRepository;
    private final SeasonService seasonService;


    // Sync today's NBA odds with pagination
    public int syncOdds() {

        LocalDate today = seasonService.getNbaToday();
        LocalDate tomorrow = today.plusDays(1);

        String cursor = null;
        int syncedCount = 0;

        do {
            // Fetch the first page or continue from the next cursor
            String url = "https://api.balldontlie.io/v2/odds"
                    + "?dates[]=" + today
                    + "&dates[]=" + tomorrow
                    + "&per_page=100";

            if (cursor != null) {
                url += "&cursor=" + cursor;
            }

            Map response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("data") == null) {
                throw new IllegalStateException("Failed to fetch odds");
            }

            List<Map> oddsList = (List<Map>) response.get("data");

            for (Map oddsData : oddsList) {

                String vendor = (String) oddsData.get("vendor");
                Number apiGameId = (Number) oddsData.get("game_id");

                if (vendor == null || apiGameId == null) continue;

                // Match odds to an existing game
                Game game = gameRepository
                        .findByGameIdFromApi(apiGameId.intValue())
                        .orElse(null);

                if (game == null) continue;

                Object spreadValue = oddsData.get("spread_home_value");
                String updatedAt = (String) oddsData.get("updated_at");

                if (spreadValue == null || updatedAt == null) continue;

                // Find the provider already selected for this game
                GameOdds odds = gameOddsRepository
                        .findFirstByGameOrderByIdAsc(game)
                        .orElse(null);

                // Keep the original provider
                if (odds != null && !odds.getVendor().equals(vendor)) {
                    continue;
                }

                if (odds == null) {
                    odds = new GameOdds();
                }

                Instant apiUpdatedAt = Instant.parse(updatedAt);

                // Do not overwrite newer odds
                if (odds.getUpdatedAt() != null
                        && !apiUpdatedAt.isAfter(odds.getUpdatedAt())) {
                    continue;
                }

                odds.setGame(game);
                odds.setVendor(vendor);
                odds.setSpreadHome(
                        new BigDecimal(spreadValue.toString())
                );
                odds.setUpdatedAt(apiUpdatedAt);

                gameOddsRepository.save(odds);
                syncedCount++;
            }

            // Read the next page cursor
            Map meta = (Map) response.get("meta");

            Object nextCursor = meta == null
                    ? null
                    : meta.get("next_cursor");

            cursor = nextCursor == null
                    ? null
                    : nextCursor.toString();

        } while (cursor != null);

        return syncedCount;
    }



}