package com.nba.nbastatsapi.service;


import com.nba.nbastatsapi.dto.TeamDTO;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final RestClient restClient;

    public void syncTeams(){
        Map respone = restClient.get()
                .uri("/teams?per_page=100")
                .retrieve()
                .body(Map.class);

        List<Map> teams = (List<Map>) respone.get("data");

        for(Map teamData : teams){
            Integer apiId = (Integer) teamData.get("id");
            Team team = teamRepository.findByTeamIdFromApi(apiId)
                    .orElse(new Team());
            team.setTeamIdFromApi(apiId);
            team.setName((String) teamData.get("name"));
            team.setFullName((String) teamData.get("full_name"));
            team.setAbbreviation((String) teamData.get("abbreviation"));
            team.setCity((String) teamData.get("city"));
            team.setConference((String) teamData.get("conference"));
            team.setDivision((String) teamData.get("division"));
            teamRepository.save(team);

    }
    }

    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(team -> TeamDTO.builder()
                        .name(team.getName())
                        .fullName(team.getFullName())
                        .abbreviation(team.getAbbreviation())
                        .city(team.getCity())
                        .conference(team.getConference())
                        .division(team.getDivision())
                        .build())
                .toList();
    }


}
