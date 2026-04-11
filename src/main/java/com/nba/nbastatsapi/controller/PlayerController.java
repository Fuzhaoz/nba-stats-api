package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.dto.PlayerDTO;
import com.nba.nbastatsapi.entity.Player;
import com.nba.nbastatsapi.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService playerService;


    @PostMapping("/sync")
    public ResponseEntity<String> syncPlayers(
            @RequestParam(defaultValue = "0") int cursor) {
        String result = playerService.syncPlayers(cursor);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlayerDTO>> searchPlayers(@RequestParam String name) {
        return ResponseEntity.ok(playerService.getPlayerByName(name));
    }
}
