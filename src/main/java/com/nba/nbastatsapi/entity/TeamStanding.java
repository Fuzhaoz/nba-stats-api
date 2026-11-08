package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "team_standings",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"team_id", "season"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private Integer season;

    private Integer conferenceRank;
    private Integer divisionRank;

    private Integer wins;
    private Integer losses;

    private String homeRecord;
    private String roadRecord;
    private String conferenceRecord;
    private String divisionRecord;
}