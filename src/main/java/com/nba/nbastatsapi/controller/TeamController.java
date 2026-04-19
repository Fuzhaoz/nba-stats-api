package com.nba.nbastatsapi.controller;


import com.nba.nbastatsapi.dto.TeamDTO;
import com.nba.nbastatsapi.dto.TeamRecordDTO;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.service.TeamService;
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
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "NBA team data and synchronization")
public class TeamController {

    private final TeamService teamService;

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
    @Operation(summary = "Get team record", description = "Return wins, losses and win percentage for a team this season")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team record returned successfully"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<TeamRecordDTO> getTeamRecord(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamRecord(id));
    }



}
