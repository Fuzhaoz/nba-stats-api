package com.nba.nbastatsapi.controller;


import com.nba.nbastatsapi.dto.PlayerDTO;
import com.nba.nbastatsapi.dto.PlayerSeasonStatsDTO;
import com.nba.nbastatsapi.dto.TeamDTO;
import com.nba.nbastatsapi.dto.TeamRecordDTO;
import com.nba.nbastatsapi.service.PlayerService;
import com.nba.nbastatsapi.service.PlayerStatsService;
import com.nba.nbastatsapi.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "NBA team data and synchronization")
public class TeamController {

    private final TeamService teamService;
    private final PlayerService playerService;
    private final PlayerStatsService playerStatsService;

    @PostMapping("/sync")
    @Operation(summary = "Sync teams", description = "Fetch all 30 NBA teams from balldontlie API and save to database")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Teams synced successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> syncTeams() {
        teamService.syncTeams();
        return ResponseEntity.ok("Teams synced successfully!");
    }

    @PostMapping("/standings/sync")
    @Operation(summary = "Sync official NBA standings")
    public ResponseEntity<String> syncStandings(
            @RequestParam int season) {

        return ResponseEntity.ok(teamService.syncStandings(season));
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Return all 30 NBA teams")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of teams returned successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }


    @GetMapping("/{id}/record")
    @Operation(
            summary = "Get team record",
            description = "Return a team's official record for a specified season"
    )
    public ResponseEntity<TeamRecordDTO> getTeamRecord(
            @PathVariable Long id,
            @RequestParam(defaultValue = "2026") int season) {

        return ResponseEntity.ok(
                teamService.getTeamRecord(id, season)
        );
    }



    @GetMapping("/standings")
    @Operation(
            summary = "Get NBA standings",
            description = "Return official NBA standings grouped by conference"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Standings returned successfully"
            )
    })
    public ResponseEntity<Map<String, List<TeamRecordDTO>>> getStandings(
            @RequestParam(defaultValue = "2026") int season) {

        return ResponseEntity.ok(teamService.getStandings(season));
    }

    @GetMapping("/{id}/roster")
    @Operation(
            summary = "Get team roster",
            description = "Return active players for a specific NBA team"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roster returned successfully"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<List<PlayerDTO>> getTeamRoster(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getTeamRoster(id));
    }
    // Get season stats for all players on a team.
    @GetMapping("/{id}/player-stats")
    @Operation(
            summary = "Get team player stats",
            description = "Get season averages for all players on a team"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Team player stats returned successfully"
    )
    public ResponseEntity<List<PlayerSeasonStatsDTO>> getTeamPlayerStats(
            @PathVariable Long id,
            @RequestParam Integer season
    ) {
        return ResponseEntity.ok(
                playerStatsService.getTeamPlayerStats(id, season)
        );
    }
}
