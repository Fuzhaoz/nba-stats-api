package com.nba.nbastatsapi.dto;

public record BoxScoreDTO(
        Long playerId,
        String playerName,
        String position,
        Long teamId,
        Boolean starter,

        String minutes,
        Integer points,
        Integer rebounds,
        Integer assists,
        Integer steals,
        Integer blocks,

        Integer fgm,
        Integer fga,
        Double fgPct,

        Integer fg3m,
        Integer fg3a,
        Double fg3Pct,

        Integer ftm,
        Integer fta,
        Double ftPct,

        Integer offensiveRebounds,
        Integer defensiveRebounds,
        Integer turnovers,
        Integer personalFouls,
        Integer plusMinus
) {
}