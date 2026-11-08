
package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamRecordDTO {

    private Long teamId;
    private String teamName;
    private String abbreviation;
    private String conference;

    private int conferenceRank;
    private int wins;
    private int losses;
    private double winPercentage;

    private String streak;
}
