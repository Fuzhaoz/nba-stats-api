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

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final RestClient restClient;
    private final TeamRepository teamRepository;



    public String syncPlayers(int cursor) {

        if (cursor != 0) {
            throw new IllegalArgumentException(
                    "Full sync must start with cursor=0"
            );
        }

        int currentCursor = 0;
        List<Map> allPlayers = new ArrayList<>();
        Set<Integer> visitedCursors = new HashSet<>();

        while (true) {

            if (!visitedCursors.add(currentCursor)) {
                throw new IllegalStateException("Repeated cursor");
            }

            Map response = restClient.get()
                    .uri("/players/active?per_page=100&cursor=" + currentCursor)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new IllegalStateException("Empty API response");
            }

            List<Map> players = (List<Map>) response.get("data");
            Map meta = (Map) response.get("meta");

            if (players == null || meta == null) {
                throw new IllegalStateException("Invalid API response");
            }

            allPlayers.addAll(players);

            Object nextCursor = meta.get("next_cursor");

            if (nextCursor == null) {
                break;
            }

            currentCursor = ((Number) nextCursor).intValue();
        }

        if (allPlayers.isEmpty()) {
            throw new IllegalStateException("Active player list is empty");
        }

        Set<Integer> activePlayerIds = new HashSet<>();

        for (Map playerData : allPlayers) {

            Integer apiId = (Integer) playerData.get("id");

            if (apiId == null) {
                throw new IllegalStateException("Player ID missing");
            }

            activePlayerIds.add(apiId);
        }


        for (Map playerData : allPlayers) {
            savePlayer(playerData);
        }


        List<Player> activePlayers =
                playerRepository.findByIsActiveTrue();

        for (Player player : activePlayers) {

            if (!activePlayerIds.contains(player.getPlayerIdFromApi())) {
                player.setActive(false);
                playerRepository.save(player);
            }
        }

        return "Sync complete! Total active players: "
                + activePlayerIds.size();
    }

    public void savePlayer(Map playerData){
        Integer apiId = (Integer) playerData.get("id");
        Player player = playerRepository.findByPlayerIdFromApi(apiId).orElse(new Player());
        player.setActive(true);
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
                .teamName(player.getTeam() != null ? player.getTeam().getName() : null)
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
    public List<PlayerDTO> getTeamRoster(Long teamId) {
        return playerRepository
                .findByTeam_IdAndIsActiveTrue(teamId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
}
