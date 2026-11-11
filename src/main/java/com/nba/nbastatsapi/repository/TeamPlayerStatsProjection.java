package com.nba.nbastatsapi.repository;

public interface TeamPlayerStatsProjection
        extends PlayerSeasonStatsProjection {

    Long getPlayerId();
}