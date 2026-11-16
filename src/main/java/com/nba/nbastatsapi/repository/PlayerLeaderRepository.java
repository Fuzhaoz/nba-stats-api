package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.Player;
import com.nba.nbastatsapi.entity.PlayerLeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerLeaderRepository
        extends JpaRepository<PlayerLeader, Long> {

    Optional<PlayerLeader> findByPlayerAndSeasonAndSeasonTypeAndStatType(
            Player player,
            Integer season,
            String seasonType,
            String statType
    );

    List<PlayerLeader> findBySeasonAndSeasonTypeAndStatTypeOrderByRankAsc(
            Integer season,
            String seasonType,
            String statType
    );
}