package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class TeamRecordDTO {
    private String teamName;
    private String conference;
    private String division;
    private int wins;
    private int losses;
    private double winPercentage;
    private int homeWins;
    private int homeLosses;
    private int awayWins;
    private int awayLosses;
}
