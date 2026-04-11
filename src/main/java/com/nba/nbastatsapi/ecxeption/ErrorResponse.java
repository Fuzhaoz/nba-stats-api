package com.nba.nbastatsapi.ecxeption;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;

}
