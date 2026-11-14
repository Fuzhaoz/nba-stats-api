package com.nba.nbastatsapi.dto;

public record PredictionStatsResponse(
        long totalPredictions,
        long wins,
        long losses,
        double winRate
) {
}