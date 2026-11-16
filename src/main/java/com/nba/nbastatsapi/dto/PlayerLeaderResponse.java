
package com.nba.nbastatsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerLeaderResponse {

    private Integer playerId;
    private String fullName;
    private Integer rank;
    private Double value;
    private Integer gamesPlayed;
}
