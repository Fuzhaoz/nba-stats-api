package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.dto.GameDTO;
import com.nba.nbastatsapi.entity.Game;
import com.nba.nbastatsapi.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncGames(
            @RequestParam(defaultValue = "0") int cursor) {
        String result = gameService.syncGames(cursor);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today")
    public ResponseEntity<List<GameDTO>> getTodayGames() {
        return ResponseEntity.ok(gameService.getTodayGames());
    }


    @GetMapping
    public ResponseEntity<List<GameDTO>> getGamesByDate(
            @RequestParam String date) {
        return ResponseEntity.ok(gameService.getGamesByDate(date));
    }

}
