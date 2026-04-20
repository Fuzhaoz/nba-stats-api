package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.GameDTO;
import com.nba.nbastatsapi.dto.TeamRecordDTO;
import com.nba.nbastatsapi.ecxeption.ResourceNotFoundException;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.repository.GameRepository;
import com.nba.nbastatsapi.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final RestClient restClient;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    public String syncGames(int cursor) {
        Map response = restClient.get()
                .uri("/games?seasons[]=2025&per_page=100&cursor=" + cursor)
                .retrieve()
                .body(Map.class);

        List<Map> games = (List<Map>) response.get("data");
        Map meta = (Map) response.get("meta");

        for(Map gameData : games){
            saveGame(gameData);
        }
        Object nextCursor = meta.get("next_cursor");
        if (nextCursor == null) {
            return "Sync complete!";
        } else {
            return "Next cursor: " + nextCursor;
        }

    }

    private void saveGame(Map gameData) {
        Integer apiId = (Integer) gameData.get("id");
        Game game = gameRepository.findByGameIdFromApi(apiId)
                .orElse(new Game());

        game.setGameIdFromApi(apiId);
        game.setDate((String) gameData.get("date"));
        game.setSeason((Integer) gameData.get("season"));
        game.setStatus((String) gameData.get("status"));
        game.setPeriod((Integer) gameData.get("period"));
        game.setTime((String) gameData.get("time"));
        game.setPostseason((Boolean) gameData.get("postseason"));
        game.setPostponed((Boolean) gameData.get("postponed"));
        game.setHomeTeamScore((Integer) gameData.get("home_team_score"));
        game.setVisitorTeamScore((Integer) gameData.get("visitor_team_score"));
        game.setDatetime((String) gameData.get("datetime"));

        game.setHomeQ1((Integer) gameData.get("home_q1"));
        game.setHomeQ2((Integer) gameData.get("home_q2"));
        game.setHomeQ3((Integer) gameData.get("home_q3"));
        game.setHomeQ4((Integer) gameData.get("home_q4"));
        game.setHomeOt1((Integer) gameData.get("home_ot1"));
        game.setHomeOt2((Integer) gameData.get("home_ot2"));
        game.setHomeOt3((Integer) gameData.get("home_ot3"));
        game.setVisitorQ1((Integer) gameData.get("visitor_q1"));
        game.setVisitorQ2((Integer) gameData.get("visitor_q2"));
        game.setVisitorQ3((Integer) gameData.get("visitor_q3"));
        game.setVisitorQ4((Integer) gameData.get("visitor_q4"));
        game.setVisitorOt1((Integer) gameData.get("visitor_ot1"));
        game.setVisitorOt2((Integer) gameData.get("visitor_ot2"));
        game.setVisitorOt3((Integer) gameData.get("visitor_ot3"));

        Map homeTeamData = (Map) gameData.get("home_team");
        if (homeTeamData != null) {
            Integer teamApiId = (Integer) homeTeamData.get("id");
            Optional<Team> homeTeam = teamRepository.findByTeamIdFromApi(teamApiId);
            homeTeam.ifPresent(game::setHomeTeam);
        }

        Map visitorTeamData = (Map) gameData.get("visitor_team");
        if (visitorTeamData != null) {
            Integer teamApiId = (Integer) visitorTeamData.get("id");
            Optional<Team> visitorTeam = teamRepository.findByTeamIdFromApi(teamApiId);
            visitorTeam.ifPresent(game::setVisitorTeam);
        }

        gameRepository.save(game);
    }

    private GameDTO convertGameDTO(Game game){
       Team homeTeam = game.getHomeTeam();
        Team visitorTeam = game.getVisitorTeam();


        return GameDTO.builder()
                .date(game.getDate())
                .datetime(game.getDatetime())
                .status(game.getStatus())
                .postseason(game.getPostseason())
                .homeTeamName(homeTeam != null ? homeTeam.getName() : null)
                .homeTeamAbbreviation(homeTeam  != null ? homeTeam .getAbbreviation() : null)
                .homeTeamScore(game.getHomeTeamScore())
                .homeQ1(game.getHomeQ1())
                .homeQ2(game.getHomeQ2())
                .homeQ3(game.getHomeQ3())
                .homeQ4(game.getHomeQ4())
                .homeOt1(game.getHomeOt1())
                .homeOt2(game.getHomeOt2())
                .homeOt3(game.getHomeOt3())
                .visitorTeamName(visitorTeam != null ? visitorTeam.getName() : null)
                .visitorTeamAbbreviation(visitorTeam != null ? visitorTeam.getAbbreviation() : null)
                .visitorTeamScore(game.getVisitorTeamScore())
                .visitorQ1(game.getVisitorQ1())
                .visitorQ2(game.getVisitorQ2())
                .visitorQ3(game.getVisitorQ3())
                .visitorQ4(game.getVisitorQ4())
                .visitorOt1(game.getVisitorOt1())
                .visitorOt2(game.getVisitorOt2())
                .visitorOt3(game.getVisitorOt3())
                .winner(determimeWinner(game))
                .build();

    }

    private String determimeWinner(Game game){
        if(game.getHomeTeamScore() == null || game.getVisitorTeamScore() == null){
            return null;
        }else if(game.getVisitorTeamScore() > game.getHomeTeamScore()){
            return game.getVisitorTeam().getName();
        }else if(game.getHomeTeamScore() > game.getVisitorTeamScore()){
            return game.getHomeTeam().getName();
        }else {
            return null;
        }

    }



    public List<GameDTO> getTodayGames() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        return gameRepository.findByDate(yesterday).stream()
                .map(this::convertGameDTO)
                .toList();
    }

    public List<GameDTO> getGamesByDate(String date) {
        return gameRepository.findByDate(date).stream()
                .map(this::convertGameDTO)
                .toList();
    }



    public void syncTodayGames() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        Map respone = restClient.get()
                .uri("/games?dates[]=" + yesterday + "&per_page=100")
                .retrieve()
                .body(Map.class);
        List<Map> games = (List<Map>)respone.get("data");

        for(Map game : games){
            saveGame(game);
        }
        log.info("Synced {} games for today: {}", games.size(), yesterday);
    }

    public List<GameDTO> getRecentGames(Long teamId, int limit) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        return gameRepository.findRecentGamesByTeamId(teamId, limit)
                .stream()
                .map(this::convertGameDTO)
                .toList();
    }


}
