package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayerGameLogDTO {

    private Long gameId;
    private String date;

    private String opponent;
    private String result;

    private String minutes;

    private Integer points;
    private Integer rebounds;
    private Integer assists;
    private Integer steals;
    private Integer blocks;
    private Integer turnovers;

    private Integer fgm;
    private Integer fga;

    private Integer fg3m;
    private Integer fg3a;

    private Integer ftm;
    private Integer fta;
}