package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayerDTO {
    private String firstName;
    private String lastName;
    private String position;
    private String height;
    private String weight;
    private String jerseyNumber;
    private String college;
    private String country;
    private String teamName;
    private String teamAbbreviation;
    private Integer draftYear;
    private Integer draftRound;
    private Integer draftNumber;


}
