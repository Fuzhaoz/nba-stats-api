package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamMatchupDTO {

    private Long teamId;
    private String teamName;

    // Season record
    private String seasonRecord;
    private double winPercentage;

    // Scoring
    private double ppg;

    // Rebounds
    private double rpg;
    private double offensiveRpg;
    private double defensiveRpg;

    // Other averages
    private double apg;
    private double spg;
    private double bpg;
    private double turnovers;
    private double plusMinus;

    // Shooting percentages
    private double fgPct;
    private double fg3Pct;
    private double ftPct;

    // Recent performance
    private String last5Record;
    private String h2hRecord;
}