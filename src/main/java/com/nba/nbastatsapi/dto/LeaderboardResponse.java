package com.nba.nbastatsapi.dto;

public record LeaderboardResponse(
        int rank,
        String username,
        long wins,
        long losses,
        long totalPredictions,
        double winRate
) {
}