
package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.GameDTO;
import com.nba.nbastatsapi.dto.GameDetailDTO;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.GameOddsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameDetailServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameOddsRepository gameOddsRepository;

    @Mock
    private GameService gameService;

    @Mock
    private MatchupService matchupService;

    @InjectMocks
    private GameDetailService gameDetailService;

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1496L);
        game.setDatetime(
                Instant.now()
                        .plus(1, ChronoUnit.DAYS)
                        .toString()
        );
        game.setPeriod(0);
        game.setStatus("Upcoming");
    }

    @Test
    void shouldReturnGameDetailForLiveGame() {

        // Arrange
        game.setPeriod(2);
        game.setStatus("In Progress");

        GameDTO gameDTO = GameDTO.builder()
                .gameId(1496L)
                .status("In Progress")
                .homeTeamName("Celtics")
                .visitorTeamName("Magic")
                .build();

        when(gameRepository.findById(1496L))
                .thenReturn(Optional.of(game));

        when(gameService.getGameById(1496L))
                .thenReturn(gameDTO);

        // Act
        GameDetailDTO result =
                gameDetailService.getGameDetail(1496L);

        // Assert
        assertNotNull(result);
        assertSame(gameDTO, result.getGame());
        assertNull(result.getMatchup());
        assertNull(result.getSpreadHome());

        verifyNoInteractions(
                matchupService,
                gameOddsRepository
        );
    }
}
