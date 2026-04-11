package com.nba.nbastatsapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamDTO {
    private String name;
    private String fullName;
    private String abbreviation;
    private String city;
    private String conference;
    private String division;

}
