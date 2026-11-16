
package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.PlayerLeaderApiResponse;
import com.nba.nbastatsapi.entity.Player;
import com.nba.nbastatsapi.entity.PlayerLeader;
import com.nba.nbastatsapi.repository.PlayerRepository;
import com.nba.nbastatsapi.repository.PlayerLeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.HashSet;
import com.nba.nbastatsapi.dto.PlayerLeaderResponse;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerLeaderSyncService {

    private final RestClient restClient;
    private final SeasonService seasonService;
    private final PlayerRepository playerRepository;
    private final PlayerLeaderRepository playerLeaderRepository;


    public List<PlayerLeaderApiResponse.LeaderData> getPlayerLeaders(
            String statType
    ) {

        int season = seasonService.getCurrentSeason();

        List<PlayerLeaderApiResponse.LeaderData> allLeaders =
                new ArrayList<>();

        PlayerLeaderApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/leaders")
                        .queryParam("season", season)
                        .queryParam("stat_type", statType)
                        .queryParam("season_type", "regular")
                        .build()
                )
                .retrieve()
                .body(PlayerLeaderApiResponse.class);

        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "Failed to fetch player leaders"
            );
        }

        allLeaders.addAll(
                response.getData().stream()
                        .filter(leader -> leader.getRank() != null
                                && leader.getRank() <= 5)
                        .toList()
        );

        return allLeaders;
    }
    @Transactional
    public int syncPlayerLeaders(String statType) {

        int season = seasonService.getCurrentSeason();

        List<PlayerLeaderApiResponse.LeaderData> leaders =
                getPlayerLeaders(statType);

        if (leaders.isEmpty()) {
            throw new IllegalStateException(
                    "No player leaders returned for: " + statType
            );
        }

        int syncedCount = 0;

        Set<Long> currentLeaderIds = new HashSet<>();

        for (PlayerLeaderApiResponse.LeaderData leader : leaders) {

            Integer apiPlayerId = leader.getPlayer().getId();

            Player player = playerRepository
                    .findByPlayerIdFromApi(apiPlayerId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Player not found: " + apiPlayerId
                            )
                    );

            PlayerLeader playerLeader = playerLeaderRepository
                    .findByPlayerAndSeasonAndSeasonTypeAndStatType(
                            player,
                            season,
                            "regular",
                            statType
                    )
                    .orElseGet(PlayerLeader::new);

            playerLeader.setPlayer(player);
            playerLeader.setSeason(season);
            playerLeader.setSeasonType("regular");
            playerLeader.setStatType(statType);
            playerLeader.setRank(leader.getRank());
            playerLeader.setValue(leader.getValue());
            playerLeader.setGamesPlayed(leader.getGamesPlayed());

            PlayerLeader savedLeader =
                    playerLeaderRepository.save(playerLeader);

            currentLeaderIds.add(savedLeader.getId());

            syncedCount++;
        }

        List<PlayerLeader> existingLeaders = playerLeaderRepository
                .findBySeasonAndSeasonTypeAndStatTypeOrderByRankAsc(
                        season,
                        "regular",
                        statType
                );

        for (PlayerLeader existingLeader : existingLeaders) {

            if (!currentLeaderIds.contains(existingLeader.getId())) {
                playerLeaderRepository.delete(existingLeader);
            }
        }

        return syncedCount;
    }

    public List<PlayerLeaderResponse> getPlayerLeadersFromDatabase(
            String statType
    ) {

        int season = seasonService.getCurrentSeason();

        List<PlayerLeader> leaders = playerLeaderRepository
                .findBySeasonAndSeasonTypeAndStatTypeOrderByRankAsc(
                        season,
                        "regular",
                        statType
                );

        return leaders.stream()
                .map(leader -> {

                    Player player = leader.getPlayer();

                    String fullName = player.getFirstName()
                            + " " + player.getLastName();

                    return new PlayerLeaderResponse(
                            player.getPlayerIdFromApi(),
                            fullName,
                            leader.getRank(),
                            leader.getValue(),
                            leader.getGamesPlayed()
                    );
                })
                .toList();
    }
}
