package com.nba.nbastatsapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamSeasonAverageDTO {

    private Integer season;
    private TeamInfo team;

    @JsonProperty("season_type")
    private String seasonType;

    private Stats stats;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {

        private Integer w;
        private Integer l;

        @JsonProperty("w_pct")
        private Double winPercentage;

        private Double pts;
        private Double reb;
        private Double ast;
        private Double stl;
        private Double blk;
        private Double tov;

        private Double oreb;
        private Double dreb;

        @JsonProperty("fg_pct")
        private Double fgPct;

        @JsonProperty("fg3_pct")
        private Double fg3Pct;

        @JsonProperty("ft_pct")
        private Double ftPct;

        @JsonProperty("plus_minus")
        private Double plusMinus;
    }


    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamInfo {
        private Integer id;
    }


}