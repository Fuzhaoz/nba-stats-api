
package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.TeamSeasonAverageDTO;
import com.nba.nbastatsapi.dto.TeamSeasonAverageResponse;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.entity.TeamSeasonStats;
import com.nba.nbastatsapi.repository.TeamRepository;
import com.nba.nbastatsapi.repository.TeamSeasonStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamSeasonStatsService {

    private final RestClient restClient;
    private final SeasonService seasonService;
    private final TeamRepository teamRepository;
    private final TeamSeasonStatsRepository teamSeasonStatsRepository;

    public List<TeamSeasonAverageDTO> getTeamSeasonAverages() {

        int season = seasonService.getCurrentSeason();

        List<TeamSeasonAverageDTO> allAverages = new ArrayList<>();

        Integer cursor = null;

        do {
            Integer currentCursor = cursor;

            TeamSeasonAverageResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/team_season_averages/general")
                                .queryParam("season", season)
                                .queryParam("season_type", "regular")
                                .queryParam("type", "base")
                                .queryParam("per_page", 100);

                        if (currentCursor != null) {
                            builder.queryParam("cursor", currentCursor);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(TeamSeasonAverageResponse.class);

            if (response == null || response.getData() == null) {
                throw new IllegalStateException(
                        "Failed to fetch team season averages"
                );
            }

            allAverages.addAll(response.getData());

            cursor = response.getMeta() == null
                    ? null
                    : response.getMeta().getNextCursor();

        } while (cursor != null);

        return allAverages;
    }

    public int syncTeamSeasonStats() {


        List<TeamSeasonAverageDTO> averages = getTeamSeasonAverages();
        // Check how many records the external API returned
        System.out.println("API returned: " + averages.size());


        int syncedCount = 0;

        for (TeamSeasonAverageDTO average : averages) {

            Integer apiTeamId = average.getTeam().getId();

            Team team = teamRepository
                    .findByTeamIdFromApi(apiTeamId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Team not found: " + apiTeamId
                            )
                    );

            TeamSeasonStats stats = teamSeasonStatsRepository
                    .findByTeamAndSeasonAndSeasonType(
                            team,
                            average.getSeason(),
                            average.getSeasonType()
                    )
                    .orElseGet(TeamSeasonStats::new);

            stats.setTeam(team);
            stats.setSeason(average.getSeason());
            stats.setSeasonType(average.getSeasonType());

            TeamSeasonAverageDTO.Stats apiStats = average.getStats();

            stats.setWins(apiStats.getW());
            stats.setLosses(apiStats.getL());
            stats.setWinPercentage(apiStats.getWinPercentage());

            stats.setPpg(apiStats.getPts());
            stats.setRpg(apiStats.getReb());
            stats.setApg(apiStats.getAst());
            stats.setSpg(apiStats.getStl());
            stats.setBpg(apiStats.getBlk());
            stats.setTurnovers(apiStats.getTov());

            stats.setOffensiveRpg(apiStats.getOreb());
            stats.setDefensiveRpg(apiStats.getDreb());

            stats.setFgPct(apiStats.getFgPct());
            stats.setFg3Pct(apiStats.getFg3Pct());
            stats.setFtPct(apiStats.getFtPct());
            stats.setPlusMinus(apiStats.getPlusMinus());

            teamSeasonStatsRepository.save(stats);
            syncedCount++;
        }

        return syncedCount;
    }
}
