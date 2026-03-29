package com.nba.nbastatsapi;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {
    @Value("${balldontlie.api.key}")
    private String apiKey;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://api.balldontlie.io/v1")
                .defaultHeader("Authorization", apiKey)
                .build();
    }

}
