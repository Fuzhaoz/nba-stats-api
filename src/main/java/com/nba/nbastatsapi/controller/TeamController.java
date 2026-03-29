package com.nba.nbastatsapi.controller;


import com.nba.nbastatsapi.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncTeams() {
        teamService.syncTeams();
        return ResponseEntity.ok("Teams synced successfully!");
    }

}
