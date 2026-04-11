package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameDTO {
    private String date;
    private String datetime;
    private String status;
    private Boolean postseason;


    private String homeTeamName;
    private String homeTeamAbbreviation;
    private Integer homeTeamScore;
    private Integer homeQ1;
    private Integer homeQ2;
    private Integer homeQ3;
    private Integer homeQ4;
    private Integer homeOt1;
    private Integer homeOt2;
    private Integer homeOt3;


    private String visitorTeamName;
    private String visitorTeamAbbreviation;
    private Integer visitorTeamScore;
    private Integer visitorQ1;
    private Integer visitorQ2;
    private Integer visitorQ3;
    private Integer visitorQ4;
    private Integer visitorOt1;
    private Integer visitorOt2;
    private Integer visitorOt3;


}
