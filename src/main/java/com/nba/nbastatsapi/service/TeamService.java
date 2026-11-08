package com.nba.nbastatsapi.service;


import com.nba.nbastatsapi.dto.GameDTO;
import com.nba.nbastatsapi.dto.TeamDTO;
import com.nba.nbastatsapi.dto.TeamRecordDTO;
import com.nba.nbastatsapi.ecxeption.ResourceNotFoundException;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.entity.TeamStanding;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.TeamRepository;
import com.nba.nbastatsapi.repository.TeamStandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final RestClient restClient;
    private final GameRepository gameRepository;
    private final TeamStandingRepository teamStandingRepository;

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

    public String syncStandings(int season) {

        // 1. Fetch official standings
        Map response = restClient.get()
                .uri("/standings?season=" + season)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("data") == null) {
            throw new IllegalStateException("Invalid standings API response");
        }

        List<Map<String, Object>> standings =
                (List<Map<String, Object>>) response.get("data");

        if (standings.isEmpty()) {
            throw new IllegalStateException(
                    "No standings returned for season: " + season);
        }

        // 2. Save or update each team's standings
        for (Map<String, Object> standingData : standings) {

            Map<String, Object> teamData =
                    (Map<String, Object>) standingData.get("team");

            Integer teamApiId = ((Number) teamData.get("id")).intValue();

            Team team = teamRepository.findByTeamIdFromApi(teamApiId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Team not found: " + teamApiId));

            TeamStanding standing = teamStandingRepository
                    .findByTeam_IdAndSeason(team.getId(), season)
                    .orElseGet(TeamStanding::new);

            standing.setTeam(team);
            standing.setSeason(season);

            standing.setConferenceRank(
                    ((Number) standingData.get("conference_rank")).intValue());

            standing.setDivisionRank(
                    ((Number) standingData.get("division_rank")).intValue());

            standing.setWins(
                    ((Number) standingData.get("wins")).intValue());

            standing.setLosses(
                    ((Number) standingData.get("losses")).intValue());

            standing.setHomeRecord((String) standingData.get("home_record"));
            standing.setRoadRecord((String) standingData.get("road_record"));
            standing.setConferenceRecord((String) standingData.get("conference_record"));
            standing.setDivisionRecord((String) standingData.get("division_record"));

            teamStandingRepository.save(standing);
        }

        return "Standings synced successfully for season " + season
                + ". Teams: " + standings.size();
    }


    private String calculateStreak(Long teamId, int season) {

        List<Game> games = gameRepository
                .findByHomeTeamIdOrVisitorTeamId(teamId, teamId)
                .stream()
                .filter(g -> g.getSeason() != null
                        && g.getSeason() == season
                        && "Final".equals(g.getStatus())
                        && Boolean.FALSE.equals(g.getPostseason())
                        && g.getHomeTeamScore() != null
                        && g.getVisitorTeamScore() != null)
                .sorted(Comparator.comparing(
                        Game::getDate,
                        Comparator.nullsFirst(
                                Comparator.naturalOrder()
                        )
                ).reversed())
                .toList();

        if (games.isEmpty()) {
            return "-";
        }

        Boolean lastResult = null;
        int count = 0;

        for (Game game : games) {

            boolean isHome = game.getHomeTeam().getId().equals(teamId);

            int teamScore = isHome
                    ? game.getHomeTeamScore()
                    : game.getVisitorTeamScore();

            int opponentScore = isHome
                    ? game.getVisitorTeamScore()
                    : game.getHomeTeamScore();

            if (teamScore == opponentScore) {
                continue;
            }

            boolean won = teamScore > opponentScore;

            if (lastResult == null) {
                lastResult = won;
            }

            if (won != lastResult) {
                break;
            }

            count++;
        }

        if (lastResult == null) {
            return "-";
        }

        return (lastResult ? "W" : "L") + count;
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

    public TeamRecordDTO getTeamRecord(Long teamId, int season) {

        TeamStanding standing = teamStandingRepository
                .findByTeam_IdAndSeason(teamId, season)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team standing not found for team: " + teamId
                ));

        Team team = standing.getTeam();

        int wins = standing.getWins();
        int losses = standing.getLosses();

        double winPercentage = wins + losses == 0
                ? 0.0
                : Math.round(
                (double) wins / (wins + losses) * 1000.0
        ) / 1000.0;

        return TeamRecordDTO.builder()
                .teamId(team.getId())
                .teamName(team.getFullName())
                .abbreviation(team.getAbbreviation())
                .conference(team.getConference())
                .conferenceRank(standing.getConferenceRank())
                .wins(wins)
                .losses(losses)
                .winPercentage(winPercentage)
                .streak(calculateStreak(teamId, season))
                .build();
    }


    public Map<String, List<TeamRecordDTO>> getStandings(int season) {

        List<TeamStanding> standings =
                teamStandingRepository.findBySeason(season);

        List<TeamRecordDTO> records = standings.stream()
                .map(s -> {

                    Team team = s.getTeam();

                    int wins = s.getWins();
                    int losses = s.getLosses();

                    double pct = wins + losses == 0
                            ? 0.0
                            : Math.round(
                            (double) wins / (wins + losses) * 1000.0
                    ) / 1000.0;

                    return TeamRecordDTO.builder()
                            .teamId(team.getId())
                            .teamName(team.getFullName())
                            .abbreviation(team.getAbbreviation())
                            .conference(team.getConference())
                            .conferenceRank(s.getConferenceRank())
                            .wins(wins)
                            .losses(losses)
                            .winPercentage(pct)
                            .streak(calculateStreak(team.getId(), season))
                            .build();
                })
                .toList();

        List<TeamRecordDTO> east = records.stream()
                .filter(r -> "East".equals(r.getConference()))
                .sorted(Comparator.comparingInt(
                        TeamRecordDTO::getConferenceRank))
                .toList();

        List<TeamRecordDTO> west = records.stream()
                .filter(r -> "West".equals(r.getConference()))
                .sorted(Comparator.comparingInt(
                        TeamRecordDTO::getConferenceRank))
                .toList();

        return Map.of("East", east, "West", west);
    }


}
