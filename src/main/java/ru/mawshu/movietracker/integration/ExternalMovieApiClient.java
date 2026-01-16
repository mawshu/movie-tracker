package ru.mawshu.movietracker.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ExternalMovieApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public ExternalMovieApiClient(@Value("${tmdb.apiKey}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.themoviedb.org/3")
                .build();
    }

    public Map searchMovies(String query, Integer year, int page) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/search/movie")
                            .queryParam("api_key", apiKey)
                            .queryParam("query", query)
                            .queryParam("page", page);
                    if (year != null) {
                        uriBuilder.queryParam("year", year);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(Map.class);
    }

    public Map getMovieDetails(String externalId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}")
                        .queryParam("api_key", apiKey)
                        .build(externalId))
                .retrieve()
                .body(Map.class);
    }

}
