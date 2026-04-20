package com.nba.nbastatsapi.service;


import com.nba.nbastatsapi.dto.GameDTO;
import com.nba.nbastatsapi.dto.TeamDTO;
import com.nba.nbastatsapi.dto.TeamRecordDTO;
import com.nba.nbastatsapi.ecxeption.ResourceNotFoundException;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.repository.GameRepository;
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
    private final GameRepository gameRepository;

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

    public TeamRecordDTO getTeamRecord(Long teamId) {
        List<Game> allGames = gameRepository.findByHomeTeamIdOrVisitorTeamId(teamId, teamId);
        List<Game> homeGames = gameRepository.findByHomeTeamId(teamId);
        List<Game> awayGames = gameRepository.findByVisitorTeamId(teamId);
        List<Game> finishedGames = allGames.stream()
                .filter(g -> "Final".equals(g.getStatus())&& Boolean.FALSE.equals(g.getPostseason()))
                .toList();

        List<Game> finishedHomeGames = homeGames.stream()
                .filter(g -> "Final".equals(g.getStatus())&& Boolean.FALSE.equals(g.getPostseason()))
                .toList();

        List<Game> finishedAwayGames = awayGames.stream()
                .filter(g -> "Final".equals(g.getStatus())&& Boolean.FALSE.equals(g.getPostseason()))
                .toList();


        int homeWins = (int) finishedHomeGames.stream()
                .filter(g -> g.getHomeTeamScore() > g.getVisitorTeamScore())
                .count();
        int homeLosses = finishedHomeGames.size() - homeWins;

        int awayWins = (int) finishedAwayGames.stream()
                .filter(g -> g.getVisitorTeamScore() > g.getHomeTeamScore())
                .count();
        int awayLosses = finishedAwayGames.size() - awayWins;

        int wins = homeWins + awayWins;
        int losses = homeLosses + awayLosses;

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));


        double winPercentage = finishedGames.isEmpty() ? 0.0 :
                Math.round((double) wins / finishedGames.size() * 1000.0) / 1000.0;


        return TeamRecordDTO.builder()
                .teamName(team.getFullName())
                .conference(team.getConference())
                .division(team.getDivision())
                .wins(wins)
                .losses(losses)
                .winPercentage(winPercentage)
                .homeWins(homeWins)
                .homeLosses(homeLosses)
                .awayWins(awayWins)
                .awayLosses(awayLosses)
                .build();

    }

    public Map<String, List<TeamRecordDTO>> getStandings(){
        List<Team> allTeams = teamRepository.findAll();
        List<TeamRecordDTO> allRecords = allTeams.stream()
                .map(t -> getTeamRecord(t.getId())) //convert Team to TeamRecordDTO
                .toList();

        List<TeamRecordDTO> sortedRecords = allRecords.stream()
                .sorted((a, b) -> Double.compare(b.getWinPercentage(), a.getWinPercentage()))
                .toList();

        List<TeamRecordDTO> east = sortedRecords.stream()
                .filter(r -> "East".equals(r.getConference()))
                .toList();

        List<TeamRecordDTO> west = sortedRecords.stream()
                .filter(r -> "West".equals(r.getConference()))
                .toList();

        return Map.of("East",east,  "West",west );


    }

}
