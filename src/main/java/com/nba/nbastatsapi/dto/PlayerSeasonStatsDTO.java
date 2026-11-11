package com.nba.nbastatsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayerSeasonStatsDTO {

    private Long playerId;
    private String playerName;
    private Integer season;

    private Long gamesPlayed;

    private Double ppg;
    private Double rpg;
    private Double apg;
    private Double spg;
    private Double bpg;
    private Double tov;

    private Double fgPct;
    private Double fg3Pct;
    private Double ftPct;
    private Double mpg;
}