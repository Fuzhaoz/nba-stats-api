package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.GameDTO;
import com.nba.nbastatsapi.dto.GameDetailDTO;
import com.nba.nbastatsapi.ecxeption.ResourceNotFoundException;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.GameOddsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nba.nbastatsapi.dto.GameMatchupDTO;
import com.nba.nbastatsapi.entity.GameOdds;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GameDetailService {

    private final GameRepository gameRepository;
    private final GameOddsRepository gameOddsRepository;
    private final GameService gameService;
    private final MatchupService matchupService;

    public GameDetailDTO getGameDetail(Long gameId) {

        // Find the game in the database
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Game not found: " + gameId
                        )
                );


        GameDTO gameDTO = gameService.getGameById(gameId);


        boolean upcoming = isUpcoming(game);


        GameMatchupDTO matchup = null;
        BigDecimal spreadHome = null;

        if (upcoming) {

            // Retrieve the matchup analysis
            try {
                matchup = matchupService.getGameMatchup(gameId);
            } catch (ResourceNotFoundException e) {
                // Missing season stats should not prevent game details
                if (e.getMessage() == null
                        || !e.getMessage().contains("season stats not found")) {
                    throw e;
                }
            }

            // Retrieve the stored odds for this game
            spreadHome = gameOddsRepository
                    .findFirstByGameOrderByIdAsc(game)
                    .map(GameOdds::getSpreadHome)
                    .orElse(null);
        }

        return GameDetailDTO.builder()
                .game(gameDTO)
                .matchup(matchup)
                .spreadHome(spreadHome)
                .build();
    }

    private boolean isUpcoming(Game game) {


        if (Boolean.TRUE.equals(game.getPostponed())) {
            return false;
        }

        if ("Final".equalsIgnoreCase(game.getStatus())) {
            return false;
        }
        // A game that has started cannot be Upcoming
        if (game.getPeriod() != null && game.getPeriod() > 0) {
            return false;
        }

        if (game.getDatetime() == null) {
            return false;
        }

        Instant gameStartTime = Instant.parse(game.getDatetime());

        return Instant.now().isBefore(gameStartTime);
    }
}