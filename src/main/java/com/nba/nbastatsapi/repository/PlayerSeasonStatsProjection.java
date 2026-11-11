package com.nba.nbastatsapi.repository;

public interface PlayerSeasonStatsProjection {

    Long getGamesPlayed();
    String getPlayerName();
    Long getTotalPoints();
    Long getTotalRebounds();
    Long getTotalAssists();
    Long getTotalSteals();
    Long getTotalBlocks();
    Long getTotalTurnovers();

    Long getTotalFgm();
    Long getTotalFga();

    Long getTotalFg3m();
    Long getTotalFg3a();

    Long getTotalFtm();
    Long getTotalFta();
    Long getTotalSeconds();
}
