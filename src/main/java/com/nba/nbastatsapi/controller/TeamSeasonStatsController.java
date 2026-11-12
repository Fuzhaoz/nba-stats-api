
package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.dto.TeamSeasonAverageDTO;
import com.nba.nbastatsapi.service.TeamSeasonStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/team-stats")
@RequiredArgsConstructor
public class TeamSeasonStatsController {

    private final TeamSeasonStatsService teamSeasonStatsService;

    @GetMapping("/season-averages")
    public List<TeamSeasonAverageDTO> getTeamSeasonAverages() {
        return teamSeasonStatsService.getTeamSeasonAverages();
    }
    @PostMapping("/sync")
    public String syncTeamSeasonStats() {

        int count = teamSeasonStatsService.syncTeamSeasonStats();

        return "Successfully synced " + count + " team season stats";
    }

}
