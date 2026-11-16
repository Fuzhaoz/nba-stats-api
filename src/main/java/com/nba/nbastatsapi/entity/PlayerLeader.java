package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "player_leaders",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "player_id",
                        "season",
                        "season_type",
                        "stat_type"
                }
        )
)
@Getter
@Setter
public class PlayerLeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer season;

    @Column(name = "season_type", nullable = false)
    private String seasonType;

    @Column(name = "stat_type", nullable = false)
    private String statType;

    @Column(name = "leader_rank", nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Double value;

    @Column(name = "games_played")
    private Integer gamesPlayed;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}