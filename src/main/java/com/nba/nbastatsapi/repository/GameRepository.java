package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameIdFromApi(Integer gameIdFromApi);
    List<Game> findByDate(String date);
    List<Game> findBySeason(Integer season);
    List<Game> findByHomeTeamIdOrVisitorTeamId(Long homeTeamId, Long visitorTeamId);
}
