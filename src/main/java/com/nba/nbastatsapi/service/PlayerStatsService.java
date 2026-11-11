package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.PlayerGameLogDTO;
import com.nba.nbastatsapi.dto.PlayerSeasonStatsDTO;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.PlayerGameStats;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.repository.PlayerGameStatsRepository;
import com.nba.nbastatsapi.repository.PlayerSeasonStatsProjection;
import com.nba.nbastatsapi.repository.TeamPlayerStatsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerStatsService {

    private final PlayerGameStatsRepository statsRepository;

    public PlayerSeasonStatsDTO getSeasonStats(
            Long playerId,
            Integer season
    ) {
        PlayerSeasonStatsProjection stats =
                statsRepository.findSeasonStats(playerId, season);

        return toSeasonStatsDTO(playerId, season, stats);
    }

    private double average(long total, long count) {
        if (count == 0) {
            return 0.0;
        }
        return Math.round((double) total / count * 10.0) / 10.0;
    }

    private double percentage(long made, long attempted) {
        if (attempted == 0) {
            return 0.0;
        }
        return Math.round((double) made / attempted * 1000.0) / 10.0;
    }


    public List<PlayerGameLogDTO> getPlayerGameLogs(
            Long playerId,
            Integer season
    ) {
        List<PlayerGameStats> statsList =
                statsRepository.findPlayerGameLogs(playerId, season);

        return statsList.stream()
                .map(this::toGameLogDTO)
                .toList();
    }
    private PlayerGameLogDTO toGameLogDTO(PlayerGameStats stats) {

        Game game = stats.getGame();
        Team playerTeam = stats.getTeam();

        boolean isHome = game.getHomeTeam().getId()
                .equals(playerTeam.getId());

        Team opponent = isHome
                ? game.getVisitorTeam()
                : game.getHomeTeam();

        Integer ourScore = isHome
                ? game.getHomeTeamScore()
                : game.getVisitorTeamScore();

        Integer opponentScore = isHome
                ? game.getVisitorTeamScore()
                : game.getHomeTeamScore();

        String result;

        if (ourScore == null || opponentScore == null) {
            result = null;
        } else if (ourScore > opponentScore) {
            result = "W";
        } else if (ourScore < opponentScore) {
            result = "L";
        } else {
            result = "T";
        }

        return PlayerGameLogDTO.builder()
                .gameId(game.getId())
                .date(game.getDate())
                .opponent(opponent.getAbbreviation())
                .result(result)
                .minutes(stats.getMinutes())
                .points(stats.getPoints())
                .rebounds(stats.getRebounds())
                .assists(stats.getAssists())
                .steals(stats.getSteals())
                .blocks(stats.getBlocks())
                .turnovers(stats.getTurnovers())
                .fgm(stats.getFgm())
                .fga(stats.getFga())
                .fg3m(stats.getFg3m())
                .fg3a(stats.getFg3a())
                .ftm(stats.getFtm())
                .fta(stats.getFta())
                .build();
    }

    private PlayerSeasonStatsDTO toSeasonStatsDTO(
            Long playerId,
            Integer season,
            PlayerSeasonStatsProjection stats
    ) {
        if (stats == null || stats.getGamesPlayed() == 0) {
            return null;
        }

        long gp = stats.getGamesPlayed();

        return PlayerSeasonStatsDTO.builder()
                .playerId(playerId)
                .playerName(stats.getPlayerName())
                .season(season)
                .gamesPlayed(gp)
                .ppg(average(stats.getTotalPoints(), gp))
                .rpg(average(stats.getTotalRebounds(), gp))
                .apg(average(stats.getTotalAssists(), gp))
                .spg(average(stats.getTotalSteals(), gp))
                .bpg(average(stats.getTotalBlocks(), gp))
                .tov(average(stats.getTotalTurnovers(), gp))
                .mpg(average(stats.getTotalSeconds(), gp * 60))
                .fgPct(percentage(stats.getTotalFgm(), stats.getTotalFga()))
                .fg3Pct(percentage(stats.getTotalFg3m(), stats.getTotalFg3a()))
                .ftPct(percentage(stats.getTotalFtm(), stats.getTotalFta()))
                .build();
    }
    // Get season stats for all players on a team.
    public List<PlayerSeasonStatsDTO> getTeamPlayerStats(
            Long teamId,
            Integer season
    ) {
        List<TeamPlayerStatsProjection> statsList =
                statsRepository.findTeamPlayerStats(teamId, season);

        return statsList.stream()
                .map(stats -> toSeasonStatsDTO(
                        stats.getPlayerId(),
                        season,
                        stats
                ))
                .toList();
    }
}