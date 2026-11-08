package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.TeamStanding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamStandingRepository extends JpaRepository<TeamStanding, Long> {

    Optional<TeamStanding> findByTeam_IdAndSeason(Long teamId, Integer season);

    List<TeamStanding> findBySeason(Integer season);
}