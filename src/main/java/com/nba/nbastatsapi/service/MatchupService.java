
package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.GameMatchupDTO;
import com.nba.nbastatsapi.dto.TeamMatchupDTO;
import com.nba.nbastatsapi.ecxeption.ResourceNotFoundException;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.entity.TeamSeasonStats;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.TeamSeasonStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchupService {

    private final GameRepository gameRepository;
    private final TeamSeasonStatsRepository teamSeasonStatsRepository;


    @Transactional(readOnly = true)
    public GameMatchupDTO getGameMatchup(Long gameId) {

        // Find the requested game or throw a resource not found exception
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Game not found: " + gameId)
                );

        // Find home team season stats
        TeamSeasonStats homeStats = teamSeasonStatsRepository
                .findByTeamAndSeasonAndSeasonType(
                        game.getHomeTeam(),
                        game.getSeason(),
                        "regular"
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Home team season stats not found for season: " + game.getSeason()
                ));

        TeamSeasonStats visitorStats = teamSeasonStatsRepository
                .findByTeamAndSeasonAndSeasonType(
                        game.getVisitorTeam(),
                        game.getSeason(),
                        "regular"
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Visitor team season stats not found for season: " + game.getSeason()
                ));
        return GameMatchupDTO.builder()
                .gameId(game.getId())
                .homeTeam(toTeamMatchupDTO(
                        game.getHomeTeam(),
                        game.getVisitorTeam(),
                        homeStats,
                        game.getSeason(),
                        game.getDate()
                ))
                .visitorTeam(toTeamMatchupDTO(
                        game.getVisitorTeam(),
                        game.getHomeTeam(),
                        visitorStats,
                        game.getSeason(),
                        game.getDate()
                ))
                .build();
    }
    // Convert team season statistics and matchup records into a DTO
    private TeamMatchupDTO toTeamMatchupDTO(
            Team team,
            Team opponent,
            TeamSeasonStats stats,
            Integer season,
            String gameDate
    ) {

        String seasonRecord = stats.getWins() != null
                && stats.getLosses() != null
                ? stats.getWins() + "-" + stats.getLosses()
                : null;

        return TeamMatchupDTO.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .seasonRecord(seasonRecord)
                .winPercentage(stats.getWinPercentage())
                .ppg(stats.getPpg())
                .rpg(stats.getRpg())
                .offensiveRpg(stats.getOffensiveRpg())
                .defensiveRpg(stats.getDefensiveRpg())
                .apg(stats.getApg())
                .spg(stats.getSpg())
                .bpg(stats.getBpg())
                .turnovers(stats.getTurnovers())
                .plusMinus(stats.getPlusMinus())
                .fgPct(stats.getFgPct())
                .fg3Pct(stats.getFg3Pct())
                .ftPct(stats.getFtPct())
                .last5Record(calculateLast5Record(team, gameDate))
                .h2hRecord(calculateH2HRecord(team, opponent, season, gameDate))
                .build();
    }


    private String calculateLast5Record(Team team, String gameDate) {

        List<Game> recentGames = gameRepository.findLast5GamesBeforeDate(
                team.getId(),
                gameDate
        );

        int wins = 0;
        int losses = 0;

        for (Game game : recentGames) {

            boolean isHomeTeam = game.getHomeTeam().getId().equals(team.getId());

            Integer teamScore = isHomeTeam
                    ? game.getHomeTeamScore()
                    : game.getVisitorTeamScore();

            Integer opponentScore = isHomeTeam
                    ? game.getVisitorTeamScore()
                    : game.getHomeTeamScore();

            if (teamScore == null || opponentScore == null) {
                continue;
            }

            if (teamScore > opponentScore) {
                wins++;
            } else if (teamScore < opponentScore) {
                losses++;
            }
        }

        return wins + "-" + losses;
    }

    // Calculate the head-to-head record between two teams in the current season
    private String calculateH2HRecord(
            Team team,
            Team opponent,
            Integer season,
            String gameDate
    ) {
        List<Game> h2hGames = gameRepository.findHeadToHeadGamesBeforeDate(
                team.getId(),
                opponent.getId(),
                season,
                gameDate
        );

        int wins = 0;
        int losses = 0;

        for (Game game : h2hGames) {

            boolean isHomeTeam = game.getHomeTeam().getId().equals(team.getId());

            Integer teamScore = isHomeTeam
                    ? game.getHomeTeamScore()
                    : game.getVisitorTeamScore();

            Integer opponentScore = isHomeTeam
                    ? game.getVisitorTeamScore()
                    : game.getHomeTeamScore();

            if (teamScore == null || opponentScore == null) {
                continue;
            }

            if (teamScore > opponentScore) {
                wins++;
            } else if (teamScore < opponentScore) {
                losses++;
            }
        }

        return wins + "-" + losses;
    }


}
