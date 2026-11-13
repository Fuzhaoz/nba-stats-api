package com.nba.nbastatsapi.dto;

import com.nba.nbastatsapi.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private Role role;
}