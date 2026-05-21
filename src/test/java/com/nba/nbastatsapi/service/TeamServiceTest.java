package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.TeamDTO;
import com.nba.nbastatsapi.dto.TeamRecordDTO;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {
    @Mock
    private TeamRepository teamRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private TeamService teamService;

    @Test
    void testSetup() {
        System.out.println("Mockito is working");
    }

    @Test
    void shouldReturnAllTeams() {
        // Fake data
        Team team = new Team();
        team.setName("Lakers");
        team.setFullName("Los Angeles Lakers");
        team.setAbbreviation("LAL");
        team.setCity("Los Angeles");
        team.setConference("West");
        team.setDivision("Pacific");

        when(teamRepository.findAll()).thenReturn(List.of(team));

        List<TeamDTO> result = teamService.getAllTeams();

        assertEquals(1, result.size());
        assertEquals("Lakers", result.get(0).getName());
    }

    @Test
    void shouldCalculateTeamRecordCorrectly() {
        Long teamId = 1L;

        Team team = new Team();
        team.setId(teamId);
        team.setFullName("Los Angeles Lakers");
        team.setConference("West");
        team.setDivision("Pacific");

        Game homeWin = new Game();
        homeWin.setStatus("Final");
        homeWin.setPostseason(false);
        homeWin.setHomeTeamScore(120);
        homeWin.setVisitorTeamScore(100);

        Game homeLoss = new Game();
        homeLoss.setStatus("Final");
        homeLoss.setPostseason(false);
        homeLoss.setHomeTeamScore(90);
        homeLoss.setVisitorTeamScore(110);

        Game awayWin = new Game();
        awayWin.setStatus("Final");
        awayWin.setPostseason(false);
        awayWin.setHomeTeamScore(95);
        awayWin.setVisitorTeamScore(105);

        when(gameRepository.findByHomeTeamIdOrVisitorTeamId(teamId, teamId))
                .thenReturn(List.of(homeWin, homeLoss, awayWin));

        when(gameRepository.findByHomeTeamId(teamId))
                .thenReturn(List.of(homeWin, homeLoss));

        when(gameRepository.findByVisitorTeamId(teamId))
                .thenReturn(List.of(awayWin));

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));

        TeamRecordDTO result = teamService.getTeamRecord(teamId);

        assertEquals("Los Angeles Lakers", result.getTeamName());
        assertEquals("West", result.getConference());
        assertEquals("Pacific", result.getDivision());

        assertEquals(2, result.getWins());
        assertEquals(1, result.getLosses());
        assertEquals(0.667, result.getWinPercentage());

        assertEquals(1, result.getHomeWins());
        assertEquals(1, result.getHomeLosses());
        assertEquals(1, result.getAwayWins());
        assertEquals(0, result.getAwayLosses());
    }
}
