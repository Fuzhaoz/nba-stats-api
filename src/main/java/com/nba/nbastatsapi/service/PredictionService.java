package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.LeaderboardResponse;
import com.nba.nbastatsapi.dto.PredictionStatsResponse;
import com.nba.nbastatsapi.entity.*;
import com.nba.nbastatsapi.repository.GameOddsRepository;
import com.nba.nbastatsapi.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.nba.nbastatsapi.dto.PredictionResponse;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final GameOddsRepository gameOddsRepository;

    // Check whether predictions are still allowed
    public boolean isPredictionOpen(Game game) {

        if (game.getDatetime() == null) {
            return false;
        }

        Instant gameStartTime = Instant.parse(game.getDatetime());
        Instant now = Instant.now();

        return now.isBefore(gameStartTime);
    }
    // Create or update a user's prediction before tipoff
    @Transactional
    public Prediction submitPrediction(
            User user,
            Game game,
            PredictionSide side) {

        if (!isPredictionOpen(game)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Prediction is closed"
            );
        }

        GameOdds odds = gameOddsRepository
                .findFirstByGameOrderByIdAsc(game)
                .orElseThrow(() ->
                        new IllegalStateException("Odds not available"));

        Prediction prediction = predictionRepository
                .findByUserAndGame(user, game)
                .orElseGet(Prediction::new);

        if (prediction.getId() == null) {
            prediction.setUser(user);
            prediction.setGame(game);
        }

        prediction.setSide(side);
        prediction.setSpreadHomeSnapshot(odds.getSpreadHome());
        prediction.setStatus(PredictionStatus.PENDING);
        prediction.setUpdatedAt(Instant.now());

        return predictionRepository.save(prediction);
    }

    // Determine prediction result using the final score and spread
    private PredictionStatus calculateResult(Prediction prediction) {

        Game game = prediction.getGame();

        int homeScore = game.getHomeTeamScore();
        int awayScore = game.getVisitorTeamScore();

        BigDecimal adjustedMargin = BigDecimal.valueOf(homeScore - awayScore)
                .add(prediction.getSpreadHomeSnapshot());

        boolean homeCovers = adjustedMargin.signum() > 0;

        if (prediction.getSide() == PredictionSide.HOME) {
            return homeCovers ? PredictionStatus.WIN : PredictionStatus.LOSS;
        }

        return homeCovers ? PredictionStatus.LOSS : PredictionStatus.WIN;
    }

    // Find predictions waiting for settlement
    public List<Prediction> getPendingPredictions() {
        return predictionRepository.findByStatus(PredictionStatus.PENDING);
    }

    // Settle pending predictions after games finish
    @Transactional
    public int settlePredictions() {

        List<Prediction> pendingPredictions = getPendingPredictions();
        int settledCount = 0;

        for (Prediction prediction : pendingPredictions) {

            Game game = prediction.getGame();

            // Skip games that are not finished
            if (!"Final".equalsIgnoreCase(game.getStatus())) {
                continue;
            }

            // Skip games without final scores
            if (game.getHomeTeamScore() == null ||
                    game.getVisitorTeamScore() == null) {
                continue;
            }

            // Calculate and save the result
            prediction.setStatus(calculateResult(prediction));
            prediction.setUpdatedAt(Instant.now());

            predictionRepository.save(prediction);
            settledCount++;
        }

        return settledCount;
    }
    // Get the user's prediction history as DTOs
    @Transactional(readOnly = true)
    public List<PredictionResponse> getMyPredictions(User user) {

        List<Prediction> predictions =
                predictionRepository.findByUserOrderByCreatedAtDesc(user);

        return predictions.stream()
                .map(this::toResponse)
                .toList();
    }
    //covert to DTO
    private PredictionResponse toResponse(Prediction prediction) {
        return new PredictionResponse(
                prediction.getId(),
                prediction.getGame().getGameIdFromApi(),
                prediction.getSide(),
                prediction.getSpreadHomeSnapshot(),
                prediction.getStatus(),
                prediction.getCreatedAt()
        );
    }
    // Calculate prediction statistics for a user
    @Transactional(readOnly = true)
    public PredictionStatsResponse getMyPredictionStats(User user) {

        // Find all predictions for the user
        List<Prediction> predictions =
                predictionRepository.findByUserOrderByCreatedAtDesc(user);


        long totalPredictions = predictions.size();


        long wins = predictions.stream()
                .filter(p -> p.getStatus() == PredictionStatus.WIN)
                .count();


        long losses = predictions.stream()
                .filter(p -> p.getStatus() == PredictionStatus.LOSS)
                .count();


        long settledPredictions = wins + losses;

        double winRate = settledPredictions == 0
                ? 0.0
                : (double) wins / settledPredictions * 100;

        return new PredictionStatsResponse(
                totalPredictions,
                wins,
                losses,
                winRate
        );
    }
    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboard() {

        // Get users with at least 5 settled predictions
        List<Object[]> results = predictionRepository.findLeaderboardStats(
                PredictionStatus.WIN,
                PredictionStatus.LOSS
        );

        // Convert database results into leaderboard responses
        List<LeaderboardResponse> leaderboard = new ArrayList<>();

        for (Object[] row : results) {

            String username = (String) row[1];
            long wins = ((Number) row[2]).longValue();
            long losses = ((Number) row[3]).longValue();

            long totalPredictions = wins + losses;
            double winRate = (double) wins / totalPredictions * 100;

            leaderboard.add(new LeaderboardResponse(
                    0,
                    username,
                    wins,
                    losses,
                    totalPredictions,
                    winRate
            ));
        }

        // Sort by win rate, then by number of wins
        leaderboard.sort(
                Comparator.comparingDouble(LeaderboardResponse::winRate)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingLong(LeaderboardResponse::wins)
                                        .reversed()
                        )
        );

        // Assign leaderboard ranks
        List<LeaderboardResponse> rankedLeaderboard = new ArrayList<>();

        for (int i = 0; i < leaderboard.size(); i++) {

            LeaderboardResponse player = leaderboard.get(i);

            rankedLeaderboard.add(new LeaderboardResponse(
                    i + 1,
                    player.username(),
                    player.wins(),
                    player.losses(),
                    player.totalPredictions(),
                    player.winRate()
            ));
        }

        return rankedLeaderboard;
    }

}