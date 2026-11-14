package com.nba.nbastatsapi.dto;

import com.nba.nbastatsapi.entity.PredictionSide;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PredictionRequest {

    // NBA game ID from the external API
    @NotNull
    private Integer gameId;

    // Selected team: HOME or AWAY
    @NotNull
    private PredictionSide side;
}