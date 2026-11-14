
package com.nba.nbastatsapi.controller;

import com.nba.nbastatsapi.service.OddsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/odds")
@RequiredArgsConstructor
public class OddsController {

    private final OddsService oddsService;

    // Manually sync NBA odds from the external API
    @PostMapping("/sync")
    public ResponseEntity<String> syncOdds() {

        int count = oddsService.syncOdds();

        return ResponseEntity.ok(
                "Odds sync complete. Updated records: " + count
        );
    }
}
