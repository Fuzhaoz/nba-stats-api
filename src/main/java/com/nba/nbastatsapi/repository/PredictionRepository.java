
package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Prediction;
import com.nba.nbastatsapi.entity.PredictionStatus;
import com.nba.nbastatsapi.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Find a user's prediction for a specific game
    Optional<Prediction> findByUserAndGame(User user, Game game);

    // Find all predictions made by a user, newest first
    List<Prediction> findByUserOrderByCreatedAtDesc(User user);

    // Find predictions by their settlement status
    List<Prediction> findByStatus(PredictionStatus status);

    @Query("""
    SELECT p.user.id,
           p.user.username,
           SUM(CASE WHEN p.status = :winStatus THEN 1L ELSE 0L END),
           SUM(CASE WHEN p.status = :lossStatus THEN 1L ELSE 0L END)
    FROM Prediction p
    GROUP BY p.user.id, p.user.username
    HAVING SUM(CASE WHEN p.status IN (:winStatus, :lossStatus)
                    THEN 1L ELSE 0L END) >= 5
    """)
    List<Object[]> findLeaderboardStats(
            @Param("winStatus") PredictionStatus winStatus,
            @Param("lossStatus") PredictionStatus lossStatus
    );
}
