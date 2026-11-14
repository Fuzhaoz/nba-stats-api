
package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.dto.LeaderboardResponse;
import com.nba.nbastatsapi.dto.PredictionRequest;
import com.nba.nbastatsapi.dto.PredictionResponse;
import com.nba.nbastatsapi.dto.PredictionStatsResponse;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Prediction;
import com.nba.nbastatsapi.entity.User;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.UserRepository;
import com.nba.nbastatsapi.service.PredictionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    // Submit or update a prediction for an NBA game
    @PostMapping
    public ResponseEntity<String> submitPrediction(
            @Valid @RequestBody PredictionRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Game game = gameRepository
                .findByGameIdFromApi(request.getGameId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Game not found"));

        Prediction prediction = predictionService.submitPrediction(
                user,
                game,
                request.getSide()
        );

        return ResponseEntity.ok(
                "Prediction saved successfully. ID: " + prediction.getId()
        );
    }

    // Get prediction history for the logged-in user
    @GetMapping("/me")
    public ResponseEntity<List<PredictionResponse>> getMyPredictions(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<PredictionResponse> predictions =
                predictionService.getMyPredictions(user);

        return ResponseEntity.ok(predictions);
    }

    // Get prediction statistics for the logged-in user
    @GetMapping("/me/stats")
    public ResponseEntity<PredictionStatsResponse> getMyPredictionStats(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        PredictionStatsResponse stats =
                predictionService.getMyPredictionStats(user);

        return ResponseEntity.ok(stats);
    }

    // Get the public prediction leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard() {

        List<LeaderboardResponse> leaderboard =
                predictionService.getLeaderboard();

        return ResponseEntity.ok(leaderboard);
    }
}
