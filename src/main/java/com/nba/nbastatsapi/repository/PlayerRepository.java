package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPlayerIdFromApi(Integer playerIdFromApi);
}
