
package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "game_odds",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"game_id", "vendor"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class GameOdds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link odds to an existing NBA game
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // Identify the odds provider
    @Column(nullable = false)
    private String vendor;

    // Store the home team's point spread
    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal spreadHome;

    // Track the last update from the odds provider
    @Column(nullable = false)
    private Instant updatedAt;
}
