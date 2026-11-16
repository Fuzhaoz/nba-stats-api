package com.nba.nbastatsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PlayerLeaderApiResponse {

    private List<LeaderData> data;

    @Data
    public static class LeaderData {
        private PlayerData player;
        private Integer rank;
        private Double value;

        @JsonProperty("stat_type")
        private String statType;

        private Integer season;

        @JsonProperty("games_played")
        private Integer gamesPlayed;
    }

    @Data
    public static class PlayerData {
        private Integer id;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;
    }
}