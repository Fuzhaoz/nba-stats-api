
package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "player_game_stats",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"game_id", "player_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // External balldontlie stats ID
    @Column(unique = true)
    private Integer statIdFromApi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // Playing time
    private String minutes;

    // Basic stats
    private Integer points;
    private Integer rebounds;
    private Integer assists;
    private Integer steals;
    private Integer blocks;

    // Field goals
    private Integer fgm;
    private Integer fga;
    private Double fgPct;

    // Three-point shots
    private Integer fg3m;
    private Integer fg3a;
    private Double fg3Pct;

    // Free throws
    private Integer ftm;
    private Integer fta;
    private Double ftPct;

    // Other stats
    private Integer offensiveRebounds;
    private Integer defensiveRebounds;
    private Integer turnovers;
    private Integer personalFouls;
    private Integer plusMinus;

    private Boolean starter;
}
