package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.dto.PlayerDTO;
import com.nba.nbastatsapi.entity.Player;
import com.nba.nbastatsapi.service.PlayerService;
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
@RequestMapping("/api/players")
@Tag(name = "Players", description = "NBA player data and synchronization")
public class PlayerController {
    private final PlayerService playerService;


    @PostMapping("/sync")
    @Operation(summary = "Sync players", description = "Fetch players from balldontlie API using cursor-based pagination and save to database")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Players synced successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> syncPlayers(
            @RequestParam(defaultValue = "0") int cursor) {
        String result = playerService.syncPlayers(cursor);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Get all players", description = "Return all players stored in the database")
    @ApiResponse(responseCode = "200", description = "List of players returned successfully")
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID", description = "Return a single player by their database ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Player found"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<PlayerDTO> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search players by name", description = "Search players by first name or last name, case-insensitive")
    @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    public ResponseEntity<List<PlayerDTO>> searchPlayers(@RequestParam String name) {
        return ResponseEntity.ok(playerService.getPlayerByName(name));
    }
}
