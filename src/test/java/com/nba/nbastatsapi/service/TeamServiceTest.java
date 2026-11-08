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


}
