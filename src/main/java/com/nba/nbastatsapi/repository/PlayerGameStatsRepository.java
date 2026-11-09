
package com.nba.nbastatsapi.repository;

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
}
