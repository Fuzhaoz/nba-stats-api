package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameMatchupDTO {

    private Long gameId;

    private TeamMatchupDTO homeTeam;

    private TeamMatchupDTO visitorTeam;
}