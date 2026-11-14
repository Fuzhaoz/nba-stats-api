
package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "predictions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "game_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who made the prediction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // NBA game being predicted
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // Selected team: HOME or AWAY
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PredictionSide side;

    // Spread at the time of prediction
    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal spreadHomeSnapshot;

    // Current prediction status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PredictionStatus status = PredictionStatus.PENDING;

    // Time when the prediction was created
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Time when the prediction was last modified
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
