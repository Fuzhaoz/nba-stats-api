
package com.nba.nbastatsapi.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nba.nbastatsapi.entity.PlayerGameStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerGameStatsRepository
        extends JpaRepository<PlayerGameStats, Long> {

    Optional<PlayerGameStats> findByGameIdAndPlayerId(
            Long gameId,
            Long playerId
    );

    List<PlayerGameStats> findByGameId(Long gameId);

    Optional<PlayerGameStats> findByStatIdFromApi(
            Integer statIdFromApi
    );
    boolean existsByGameId(Long gameId);

    @Query(value = """
    SELECT
        COUNT(*) AS "gamesPlayed",
        CONCAT(MAX(p.first_name), ' ', MAX(p.last_name)) AS "playerName",

        COALESCE(SUM(s.points), 0) AS "totalPoints",
        COALESCE(SUM(s.rebounds), 0) AS "totalRebounds",
        COALESCE(SUM(s.assists), 0) AS "totalAssists",
        COALESCE(SUM(s.steals), 0) AS "totalSteals",
        COALESCE(SUM(s.blocks), 0) AS "totalBlocks",
        COALESCE(SUM(s.turnovers), 0) AS "totalTurnovers",

        COALESCE(SUM(s.fgm), 0) AS "totalFgm",
        COALESCE(SUM(s.fga), 0) AS "totalFga",
        COALESCE(SUM(s.fg3m), 0) AS "totalFg3m",
        COALESCE(SUM(s.fg3a), 0) AS "totalFg3a",
        COALESCE(SUM(s.ftm), 0) AS "totalFtm",
        COALESCE(SUM(s.fta), 0) AS "totalFta",

        COALESCE(SUM(
            split_part(s.minutes, ':', 1)::integer * 60
            + split_part(s.minutes, ':', 2)::integer
        ), 0) AS "totalSeconds"

    FROM player_game_stats s
    JOIN games g ON s.game_id = g.id
    JOIN players p ON s.player_id = p.id
    WHERE s.player_id = :playerId
      AND g.season = :season
      AND g.status = 'Final'
      AND s.minutes ~ '^[0-9]+:[0-5][0-9]$'
      AND (
          split_part(s.minutes, ':', 1)::integer * 60
          + split_part(s.minutes, ':', 2)::integer
      ) > 0
    """, nativeQuery = true)
    PlayerSeasonStatsProjection findSeasonStats(
            @Param("playerId") Long playerId,
            @Param("season") Integer season
    );


    @Query("""
    SELECT s
    FROM PlayerGameStats s
    JOIN FETCH s.game g
    JOIN FETCH s.team t
    JOIN FETCH g.homeTeam
    JOIN FETCH g.visitorTeam
    WHERE s.player.id = :playerId
      AND g.season = :season
      AND g.status = 'Final'
    ORDER BY g.date DESC, g.id DESC
    """)
    List<PlayerGameStats> findPlayerGameLogs(
            @Param("playerId") Long playerId,
            @Param("season") Integer season
    );

    @Query(value = """
    SELECT
        s.player_id AS "playerId",
        CONCAT(MAX(p.first_name), ' ', MAX(p.last_name)) AS "playerName",

        COUNT(*) AS "gamesPlayed",

        COALESCE(SUM(s.points), 0) AS "totalPoints",
        COALESCE(SUM(s.rebounds), 0) AS "totalRebounds",
        COALESCE(SUM(s.assists), 0) AS "totalAssists",
        COALESCE(SUM(s.steals), 0) AS "totalSteals",
        COALESCE(SUM(s.blocks), 0) AS "totalBlocks",
        COALESCE(SUM(s.turnovers), 0) AS "totalTurnovers",

        COALESCE(SUM(s.fgm), 0) AS "totalFgm",
        COALESCE(SUM(s.fga), 0) AS "totalFga",
        COALESCE(SUM(s.fg3m), 0) AS "totalFg3m",
        COALESCE(SUM(s.fg3a), 0) AS "totalFg3a",
        COALESCE(SUM(s.ftm), 0) AS "totalFtm",
        COALESCE(SUM(s.fta), 0) AS "totalFta",

        COALESCE(SUM(
            split_part(s.minutes, ':', 1)::integer * 60
            + split_part(s.minutes, ':', 2)::integer
        ), 0) AS "totalSeconds"

    FROM player_game_stats s
    JOIN games g ON s.game_id = g.id
    JOIN players p ON s.player_id = p.id

    WHERE s.team_id = :teamId
      AND g.season = :season
      AND g.status = 'Final'
      AND s.minutes ~ '^[0-9]+:[0-5][0-9]$'
      AND (
          split_part(s.minutes, ':', 1)::integer * 60
          + split_part(s.minutes, ':', 2)::integer
      ) > 0

    GROUP BY s.player_id
    ORDER BY SUM(s.points) DESC
    """, nativeQuery = true)
    List<TeamPlayerStatsProjection> findTeamPlayerStats(
            @Param("teamId") Long teamId,
            @Param("season") Integer season
    );

}
