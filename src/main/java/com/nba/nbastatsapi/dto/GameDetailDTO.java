package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class GameDetailDTO {

    private GameDTO game;

    private GameMatchupDTO matchup;

    private BigDecimal spreadHome;
}