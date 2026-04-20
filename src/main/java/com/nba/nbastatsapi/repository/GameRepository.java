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
}
