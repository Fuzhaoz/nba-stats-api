package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name ="games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"homeTeam", "visitorTeam"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(unique = true)
    private Integer gameIdFromApi;

    private String date;
    private Integer season;
    private String status;
    private Integer period;
    private String time;
    private Boolean postseason;
    private Boolean postponed;
    private Integer homeTeamScore;
    private Integer visitorTeamScore;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_team_id")
    private Team visitorTeam;

    private Integer homeQ1;
    private Integer homeQ2;
    private Integer homeQ3;
    private Integer homeQ4;
    private Integer homeOt1;
    private Integer homeOt2;
    private Integer homeOt3;

    private Integer visitorQ1;
    private Integer visitorQ2;
    private Integer visitorQ3;
    private Integer visitorQ4;
    private Integer visitorOt1;
    private Integer visitorOt2;
    private Integer visitorOt3;
    private String datetime;

}
