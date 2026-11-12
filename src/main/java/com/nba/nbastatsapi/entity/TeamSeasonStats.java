
package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "team_season_stats",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"team_id", "season", "season_type"}
        )
)
@Getter
@Setter
public class TeamSeasonStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private Integer season;

    @Column(name = "season_type", nullable = false)
    private String seasonType;

    private Integer wins;
    private Integer losses;

    private Double winPercentage;

    private Double ppg;
    private Double rpg;
    private Double apg;
    private Double spg;
    private Double bpg;
    private Double turnovers;

    private Double offensiveRpg;
    private Double defensiveRpg;

    private Double fgPct;
    private Double fg3Pct;
    private Double ftPct;
    private Double plusMinus;
}
