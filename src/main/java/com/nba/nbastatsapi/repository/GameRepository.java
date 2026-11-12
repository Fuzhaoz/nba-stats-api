package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameIdFromApi(Integer gameIdFromApi);
    List<Game> findByDate(String date);
    List<Game> findBySeason(Integer season);
    List<Game> findByHomeTeamIdOrVisitorTeamId(Long homeTeamId, Long visitorTeamId);
    List<Game> findByHomeTeamId(Long homeTeamId);
    List<Game> findByVisitorTeamId(Long visitorTeamId);
    @Query("SELECT g FROM Game g WHERE (g.homeTeam.id = :teamId OR g.visitorTeam.id = :teamId) AND g.status = 'Final' ORDER BY g.date DESC LIMIT :limit")
    List<Game> findRecentGamesByTeamId(@Param("teamId") Long teamId, @Param("limit") int limit);
    List<Game> findBySeasonOrderByDateAscIdAsc(Integer season);

    @Query("""
    SELECT g FROM Game g
    WHERE (g.homeTeam.id = :teamId OR g.visitorTeam.id = :teamId)
      AND g.status = 'Final'
      AND g.date < :gameDate
    ORDER BY g.date DESC, g.id DESC
    LIMIT 5
    """)
    List<Game> findLast5GamesBeforeDate(
            @Param("teamId") Long teamId,
            @Param("gameDate") String gameDate
    );


    @Query("""
    SELECT g FROM Game g
    WHERE g.season = :season
      AND g.status = 'Final'
      AND g.date < :gameDate
      AND (
          (g.homeTeam.id = :teamAId AND g.visitorTeam.id = :teamBId)
          OR
          (g.homeTeam.id = :teamBId AND g.visitorTeam.id = :teamAId)
      )
    ORDER BY g.date DESC
    """)
    List<Game> findHeadToHeadGamesBeforeDate(
            @Param("teamAId") Long teamAId,
            @Param("teamBId") Long teamBId,
            @Param("season") Integer season,
            @Param("gameDate") String gameDate
    );
}
