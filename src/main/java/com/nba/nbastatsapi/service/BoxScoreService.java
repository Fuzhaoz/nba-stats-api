
package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.BoxScoreDTO;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Player;
import com.nba.nbastatsapi.entity.PlayerGameStats;
import com.nba.nbastatsapi.entity.Team;

import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.PlayerRepository;
import com.nba.nbastatsapi.repository.PlayerGameStatsRepository;
import com.nba.nbastatsapi.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoxScoreService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final PlayerGameStatsRepository statsRepository;
    private final RestClient restClient;

    @Transactional
    public void syncBoxScore(Integer gameIdFromApi) {

        // 1. Find the local Game entity
        Game game = gameRepository
                .findByGameIdFromApi(gameIdFromApi)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Game not found: " + gameIdFromApi
                        )
                );

        Integer cursor = null;
        int totalProcessed = 0;

        do {
            //Fetch stats from balldontlie
            final Integer currentCursor = cursor;

            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/stats")
                                .queryParam("game_ids[]", gameIdFromApi)
                                .queryParam("per_page", 100);

                        if (currentCursor != null) {
                            builder.queryParam("cursor", currentCursor);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new IllegalStateException(
                        "Box Score API returned an empty response"
                );
            }

            // 3. Extract the data array
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data =
                    (List<Map<String, Object>>) response.get("data");

            if (data == null) {
                throw new IllegalStateException(
                        "Box Score data is missing"
                );
            }

            //Process each player's stats
            for (Map<String, Object> item : data) {

                @SuppressWarnings("unchecked")
                Map<String, Object> playerData =
                        (Map<String, Object>) item.get("player");

                @SuppressWarnings("unchecked")
                Map<String, Object> teamData =
                        (Map<String, Object>) item.get("team");

                if (playerData == null || teamData == null) {
                    throw new IllegalStateException(
                            "Player or team data is missing"
                    );
                }

                Integer playerIdFromApi =
                        toInteger(playerData.get("id"));

                Integer teamIdFromApi =
                        toInteger(teamData.get("id"));

                // Find the team for THIS game
                Team team = teamRepository
                        .findByTeamIdFromApi(teamIdFromApi)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Team not found: " + teamIdFromApi
                                )
                        );

                // Find or create player
                Player player = playerRepository
                        .findByPlayerIdFromApi(playerIdFromApi)
                        .orElseGet(() ->
                                createPlayer(playerData)
                        );

                // Find existing stats or create a new record
                PlayerGameStats stats = statsRepository
                        .findByGameIdAndPlayerId(
                                game.getId(),
                                player.getId()
                        )
                        .orElseGet(PlayerGameStats::new);

                // Set entity relationships
                stats.setGame(game);
                stats.setPlayer(player);
                stats.setTeam(team);

                // map API fields to Java fields
                stats.setStatIdFromApi(
                        toInteger(item.get("id"))
                );

                stats.setMinutes(
                        (String) item.get("min")
                );

                stats.setPoints(
                        toInteger(item.get("pts"))
                );
                stats.setRebounds(
                        toInteger(item.get("reb"))
                );
                stats.setAssists(
                        toInteger(item.get("ast"))
                );
                stats.setSteals(
                        toInteger(item.get("stl"))
                );
                stats.setBlocks(
                        toInteger(item.get("blk"))
                );

                stats.setFgm(
                        toInteger(item.get("fgm"))
                );
                stats.setFga(
                        toInteger(item.get("fga"))
                );
                stats.setFgPct(
                        toDouble(item.get("fg_pct"))
                );

                stats.setFg3m(
                        toInteger(item.get("fg3m"))
                );
                stats.setFg3a(
                        toInteger(item.get("fg3a"))
                );
                stats.setFg3Pct(
                        toDouble(item.get("fg3_pct"))
                );

                stats.setFtm(
                        toInteger(item.get("ftm"))
                );
                stats.setFta(
                        toInteger(item.get("fta"))
                );
                stats.setFtPct(
                        toDouble(item.get("ft_pct"))
                );

                stats.setOffensiveRebounds(
                        toInteger(item.get("oreb"))
                );
                stats.setDefensiveRebounds(
                        toInteger(item.get("dreb"))
                );
                stats.setTurnovers(
                        toInteger(item.get("turnover"))
                );
                stats.setPersonalFouls(
                        toInteger(item.get("pf"))
                );
                stats.setPlusMinus(
                        toInteger(item.get("plus_minus"))
                );

                // 10. Insert or update the stats record
                statsRepository.save(stats);
                totalProcessed++;
            }

            // Handle API pagination
            @SuppressWarnings("unchecked")
            Map<String, Object> meta =
                    (Map<String, Object>) response.get("meta");

            cursor = meta == null
                    ? null
                    : toInteger(meta.get("next_cursor"));

        } while (cursor != null);
        if (totalProcessed == 0) {
            throw new IllegalStateException(
                    "No box score data found for game: " + gameIdFromApi
            );
        }
        System.out.println(
                "Box Score synced. API Game ID: "
                        + gameIdFromApi
                        + ", records processed: "
                        + totalProcessed
        );
    }
    //get player statstic
    @Transactional(readOnly = true)
    public List<BoxScoreDTO> getBoxScore(Long gameId) {
        return statsRepository.findByGameId(gameId)
                .stream()
                .map(this::toDTO)
                .toList();
    }
    // Create a missing player using API data
    private Player createPlayer(
            Map<String, Object> playerData
    ) {

        Player player = new Player();

        player.setPlayerIdFromApi(
                toInteger(playerData.get("id"))
        );
        player.setFirstName(
                (String) playerData.get("first_name")
        );
        player.setLastName(
                (String) playerData.get("last_name")
        );
        player.setPosition(
                (String) playerData.get("position")
        );
        player.setHeight(
                (String) playerData.get("height")
        );
        player.setWeight(
                (String) playerData.get("weight")
        );
        player.setJerseyNumber(
                (String) playerData.get("jersey_number")
        );
        player.setCollege(
                (String) playerData.get("college")
        );
        player.setCountry(
                (String) playerData.get("country")
        );
        player.setDraftYear(
                toInteger(playerData.get("draft_year"))
        );
        player.setDraftRound(
                toInteger(playerData.get("draft_round"))
        );
        player.setDraftNumber(
                toInteger(playerData.get("draft_number"))
        );

        // Player's team_id is different from game-time team.
        Integer currentTeamId =
                toInteger(playerData.get("team_id"));

        Team currentTeam = currentTeamId == null
                ? null
                : teamRepository
                .findByTeamIdFromApi(currentTeamId)
                .orElse(null);

        player.setTeam(currentTeam);
        player.setActive(false);

        return playerRepository.save(player);
    }
    //convert to Dto
    private BoxScoreDTO toDTO(PlayerGameStats stats) {
        return new BoxScoreDTO(
                stats.getPlayer().getId(),
                stats.getPlayer().getFirstName() + " " + stats.getPlayer().getLastName(),
                stats.getPlayer().getPosition(),
                stats.getTeam().getId(),
                stats.getStarter(),

                stats.getMinutes(),
                stats.getPoints(),
                stats.getRebounds(),
                stats.getAssists(),
                stats.getSteals(),
                stats.getBlocks(),

                stats.getFgm(),
                stats.getFga(),
                stats.getFgPct(),

                stats.getFg3m(),
                stats.getFg3a(),
                stats.getFg3Pct(),

                stats.getFtm(),
                stats.getFta(),
                stats.getFtPct(),

                stats.getOffensiveRebounds(),
                stats.getDefensiveRebounds(),
                stats.getTurnovers(),
                stats.getPersonalFouls(),
                stats.getPlusMinus()
        );
    }

    // Sync player starter/bench status for a specific game
    public void syncLineups(Integer gameIdFromApi) {

        // Fetch lineup data from BALLDONTLIE API
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/lineups")
                        .queryParam("game_ids[]", gameIdFromApi)
                        .queryParam("per_page", 100)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null) {
            throw new IllegalStateException(
                    "Lineups API returned an empty response"
            );
        }
        // Extract lineup records from the API response
        List<Map<String, Object>> lineups =
                (List<Map<String, Object>>) response.get("data");
        // Reject missing or empty lineup data
        if (lineups == null || lineups.isEmpty()) {
            throw new IllegalStateException(
                    "No lineup data found for game: " + gameIdFromApi
            );
        }

        Game game = gameRepository
                .findByGameIdFromApi(gameIdFromApi)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // Extract player ID and starter status from each lineup
        for (Map<String, Object> lineup : lineups) {

            Map<String, Object> player =
                    (Map<String, Object>) lineup.get("player");

            Integer playerIdFromApi =
                    ((Number) player.get("id")).intValue();

            Boolean starter = (Boolean) lineup.get("starter");

            // Find the player using their external API ID
            Player existingPlayer = playerRepository
                    .findByPlayerIdFromApi(playerIdFromApi)
                    .orElse(null);

            if (existingPlayer == null) {
                continue;
            }
            // Find the player's stats for this game
            PlayerGameStats stats = statsRepository
                    .findByGameIdAndPlayerId(game.getId(), existingPlayer.getId())
                    .orElse(null);

            if (stats == null) {
                continue;
            }
            // Update and save the player's starter status
            stats.setStarter(starter);
            statsRepository.save(stats);
        }

    }


    // Safely convert nullable numeric values
    private Integer toInteger(Object value) {
        return value == null
                ? null
                : ((Number) value).intValue();
    }

    private Double toDouble(Object value) {
        return value == null
                ? null
                : ((Number) value).doubleValue();
    }


}
