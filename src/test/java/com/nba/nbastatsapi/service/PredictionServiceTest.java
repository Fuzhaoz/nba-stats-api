package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.LeaderboardResponse;
import com.nba.nbastatsapi.entity.*;
import com.nba.nbastatsapi.service.PredictionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import com.nba.nbastatsapi.repository.PredictionRepository;
import com.nba.nbastatsapi.repository.GameOddsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {
    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private GameOddsRepository gameOddsRepository;

    @InjectMocks
    private PredictionService predictionService;

    @Test
    void homePredictionShouldWinWhenHomeCoversSpread() {

        // Arrange: Prepare a finished game
        Game game = new Game();
        game.setStatus("Final");
        game.setHomeTeamScore(115);
        game.setVisitorTeamScore(105);

        // Arrange: Prepare a HOME prediction
        Prediction prediction = new Prediction();
        prediction.setGame(game);
        prediction.setSide(PredictionSide.HOME);
        prediction.setSpreadHomeSnapshot(new BigDecimal("-7.5"));
        prediction.setStatus(PredictionStatus.PENDING);

        // Mock pending predictions from database
        when(predictionRepository.findByStatus(PredictionStatus.PENDING))
                .thenReturn(List.of(prediction));

        // Act: Execute settlement
        int settledCount = predictionService.settlePredictions();

        // Assert: Verify result
        assertEquals(PredictionStatus.WIN, prediction.getStatus());
        assertEquals(1, settledCount);

        verify(predictionRepository).save(prediction);
    }

    @Test
    void getLeaderboard_shouldSortByWinRateThenWins() {

        // Arrange: Mock leaderboard statistics
        List<Object[]> mockResults = List.of(
                new Object[]{1L, "Amy", 6L, 4L},
                new Object[]{2L, "Jack", 4L, 1L},
                new Object[]{3L, "Tony", 8L, 2L}
        );

        when(predictionRepository.findLeaderboardStats(
                PredictionStatus.WIN,
                PredictionStatus.LOSS
        )).thenReturn(mockResults);

        // Act
        List<LeaderboardResponse> leaderboard =
                predictionService.getLeaderboard();

        // Assert
        assertEquals(3, leaderboard.size());

        assertEquals("Tony", leaderboard.get(0).username());
        assertEquals(1, leaderboard.get(0).rank());
        assertEquals(80.0, leaderboard.get(0).winRate(), 0.001);

        assertEquals("Jack", leaderboard.get(1).username());
        assertEquals(2, leaderboard.get(1).rank());

        assertEquals("Amy", leaderboard.get(2).username());
        assertEquals(3, leaderboard.get(2).rank());

        // Verify repository interaction
        verify(predictionRepository).findLeaderboardStats(
                PredictionStatus.WIN,
                PredictionStatus.LOSS
        );
    }

}