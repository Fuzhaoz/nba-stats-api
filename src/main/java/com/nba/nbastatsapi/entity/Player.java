package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true)
    private Integer playerIdFromApi;

    private String firstName;
    private String lastName;
    private String position;
    private String height;
    private String weight;
    private String jerseyNumber;
    private String college;
    private String country;
    private Integer draftYear;
    private Integer draftRound;
    private Integer draftNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
