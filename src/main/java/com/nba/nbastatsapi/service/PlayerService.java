package com.nba.nbastatsapi.service;

import com.nba.nbastatsapi.dto.PlayerDTO;
import com.nba.nbastatsapi.ecxeption.ResourceNotFoundException;
import com.nba.nbastatsapi.entity.Player;
import com.nba.nbastatsapi.entity.Team;
import com.nba.nbastatsapi.repository.PlayerRepository;
import com.nba.nbastatsapi.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final RestClient restClient;
    private final TeamRepository teamRepository;


    public String syncPlayers(int cursor) {

            Map respone = restClient.get()
                    .uri("/players?per_page=100&cursor="+ cursor )
                    .retrieve()
                    .body(Map.class);
            List<Map> players = (List<Map>)respone.get("data");
            Map meta = (Map)respone.get("meta");

            for(Map playerData : players){
                savePlayer(playerData);
            }
            Object nextCursor = meta.get("next_cursor");
            if(nextCursor == null){
                return "Sync complete!";
            }else {
                return "Next cursor: " + nextCursor;
            }



    }
    public void savePlayer(Map playerData){
        Integer apiId = (Integer) playerData.get("id");
        Player player = playerRepository.findByPlayerIdFromApi(apiId).orElse(new Player());

        player.setPlayerIdFromApi(apiId);
        player.setFirstName((String) playerData.get("first_name"));
        player.setLastName((String) playerData.get("last_name"));
        player.setPosition((String) playerData.get("position"));
        player.setHeight((String) playerData.get("height"));
        player.setWeight((String) playerData.get("weight"));
        player.setJerseyNumber((String) playerData.get("jersey_number"));
        player.setCollege((String) playerData.get("college"));
        player.setCountry((String) playerData.get("country"));
        player.setDraftYear((Integer) playerData.get("draft_year"));
        player.setDraftRound((Integer) playerData.get("draft_round"));
        player.setDraftNumber((Integer) playerData.get("draft_number"));

        Map teamData = (Map) playerData.get("team");
        if (teamData != null) {
            Integer teamApiId = (Integer) teamData.get("id");
            Optional<Team> team = teamRepository.findByTeamIdFromApi(teamApiId);
            team.ifPresent(player::setTeam);
        }

        playerRepository.save(player);

    }

    private PlayerDTO convertToDTO(Player player){
        return PlayerDTO.builder().firstName(player.getFirstName())
                        .lastName(player.getLastName())
                .position(player.getPosition())
                .height(player.getHeight())
                .weight(player.getWeight())
                .jerseyNumber(player.getJerseyNumber())
                .college(player.getCollege())
                .country(player.getCountry())
                .teamName(player.getTeam().getName() != null ? player.getTeam().getAbbreviation() : null )
                .teamAbbreviation(player.getTeam() != null ? player.getTeam().getAbbreviation() : null)
                .draftYear(player.getDraftYear())
                .draftRound(player.getDraftRound())
                .draftNumber(player.getDraftNumber())
                .build();
    }



    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public PlayerDTO getPlayerById(Long id) {
        return playerRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));
    }


    public List<PlayerDTO> getPlayerByName(String name) {
        return playerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name).stream()
                .map(this::convertToDTO)
                .toList();
    }
}
