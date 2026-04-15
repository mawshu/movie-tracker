package ru.mawshu.movietracker.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.mawshu.movietracker.exception.ExternalServiceException;

import java.util.Map;

@Component
public class ExternalMovieApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public ExternalMovieApiClient(
            RestClient tmdbRestClient,
            @Value("${tmdb.apiKey}") String apiKey
    ) {
        this.restClient = tmdbRestClient;
        this.apiKey = apiKey;
    }

    public Map searchMovies(String query, Integer year, int page) {
        try {
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
        } catch (RestClientException ex) {
            throw new ExternalServiceException("TMDb service is temporarily unavailable", ex);
        }
    }

    public Map getMovieDetails(String externalId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}")
                            .queryParam("api_key", apiKey)
                            .build(externalId))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("TMDb service is temporarily unavailable", ex);
        }
    }
}