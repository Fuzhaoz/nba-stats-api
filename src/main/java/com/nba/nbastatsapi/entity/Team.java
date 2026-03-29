package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer teamIdFromApi;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fullName;

    private String abbreviation;
    private String city;
    private String conference;
    private String division;
}
