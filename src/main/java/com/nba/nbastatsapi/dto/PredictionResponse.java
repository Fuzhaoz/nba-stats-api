package com.nba.nbastatsapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.nba.nbastatsapi.entity.PredictionSide;
import com.nba.nbastatsapi.entity.PredictionStatus;

public record PredictionResponse(
        Long id,
        Integer gameId,
        PredictionSide side,
        BigDecimal spreadHomeSnapshot,
        PredictionStatus status,
        Instant createdAt
) {
}