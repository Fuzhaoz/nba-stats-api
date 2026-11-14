package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.GameOdds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameOddsRepository extends JpaRepository<GameOdds, Long> {

    // Find the latest stored odds for a game and vendor
    Optional<GameOdds> findByGameAndVendor(Game game, String vendor);
    // Find the stored odds for a game
    Optional<GameOdds> findFirstByGameOrderByIdAsc(Game game);
}