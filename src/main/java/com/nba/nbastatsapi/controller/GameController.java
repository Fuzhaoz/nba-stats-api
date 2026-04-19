package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.dto.GameDTO;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games")
@Tag(name = "Games", description = "NBA game results and synchronization")
public class GameController {
    private final GameService gameService;

    @PostMapping("/sync")
    @Operation(summary = "Sync games", description = "Fetch games from balldontlie API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Games synced successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> syncGames(
            @RequestParam(defaultValue = "0") int cursor) {
        String result = gameService.syncGames(cursor);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's games", description = "Return all NBA games scheduled for today (adjusted for Australia timezone)")
    @ApiResponse(responseCode = "200", description = "Today's games returned successfully")
    public ResponseEntity<List<GameDTO>> getTodayGames() {
        return ResponseEntity.ok(gameService.getTodayGames());
    }


    @GetMapping
    @Operation(summary = "Get games by date", description = "Return all games on a specific date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Games returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    public ResponseEntity<List<GameDTO>> getGamesByDate(
            @RequestParam String date) {
        return ResponseEntity.ok(gameService.getGamesByDate(date));
    }

}
