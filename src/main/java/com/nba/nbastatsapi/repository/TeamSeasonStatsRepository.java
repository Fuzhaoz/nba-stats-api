package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.entity.TeamSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamSeasonStatsRepository
        extends JpaRepository<TeamSeasonStats, Long> {

    Optional<TeamSeasonStats> findByTeamAndSeasonAndSeasonType(
            Team team,
            Integer season,
            String seasonType
    );
}