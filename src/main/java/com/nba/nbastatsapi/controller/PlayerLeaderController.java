
package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.dto.PlayerLeaderResponse;
import com.nba.nbastatsapi.service.PlayerLeaderSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/leaders")
@RequiredArgsConstructor
public class PlayerLeaderController {

    private final PlayerLeaderSyncService playerLeaderSyncService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncPlayerLeaders(
            @RequestParam String statType
    ) {

        int syncedCount =
                playerLeaderSyncService.syncPlayerLeaders(statType);

        return ResponseEntity.ok(
                "Successfully synced " + syncedCount
                        + " player leaders for " + statType
        );
    }

    @GetMapping
    public ResponseEntity<List<PlayerLeaderResponse>> getPlayerLeaders(
            @RequestParam String statType
    ) {

        List<PlayerLeaderResponse> leaders =
                playerLeaderSyncService.getPlayerLeadersFromDatabase(statType);

        return ResponseEntity.ok(leaders);
    }
}
