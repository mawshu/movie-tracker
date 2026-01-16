package ru.mawshu.movietracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mawshu.movietracker.domain.Movie;
import ru.mawshu.movietracker.repository.MovieRepository;
import ru.mawshu.movietracker.dto.MovieSearchItem;
import ru.mawshu.movietracker.dto.MovieResponse;
import ru.mawshu.movietracker.integration.ExternalMovieApiClient;
import ru.mawshu.movietracker.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MovieCatalogService {

    private final MovieRepository movieRepository;
    private final ExternalMovieApiClient externalMovieApiClient;

    public MovieCatalogService(MovieRepository movieRepository, ExternalMovieApiClient externalMovieApiClient) {
        this.movieRepository = movieRepository;
        this.externalMovieApiClient = externalMovieApiClient;
    }


    public Movie saveIfNotExists(Movie movie) {
        Optional<Movie> existing = movieRepository.findByExternalId(movie.getExternalId());
        if (existing.isPresent()) {
            return existing.get();
        }

        movie.setCreatedAt(LocalDateTime.now());
        return movieRepository.save(movie);
    }

    public Optional<Movie> getMovie(Long id) {
        return movieRepository.findById(id);
    }

    public List<MovieSearchItem> searchMovies(String query, Integer year, int page, int size) {
        Map response = externalMovieApiClient.searchMovies(query, year, page);

        Object resultsObj = response.get("results");
        if (!(resultsObj instanceof List<?> results)) {
            return List.of();
        }

        List<MovieSearchItem> items = new ArrayList<>();
        int limit = Math.min(size, results.size());

        for (int i = 0; i < limit; i++) {
            Object rowObj = results.get(i);
            if (!(rowObj instanceof Map<?, ?>)) continue;
            Map<String, Object> row = (Map<String, Object>) rowObj;

            Object idObj = row.get("id");
            String externalId = idObj == null ? null : String.valueOf(idObj);

            String title = (String) row.getOrDefault("title", "");
            String releaseDate = (String) row.get("release_date");
            Integer parsedYear = parseYear(releaseDate);

            String overview = (String) row.getOrDefault("overview", "");

            String posterPath = (String) row.get("poster_path");
            String posterUrl = posterPath == null ? null : "https://image.tmdb.org/t/p/w500" + posterPath;

            items.add(new MovieSearchItem(externalId, title, parsedYear, posterUrl, overview));
        }

        return items;
    }

    private Integer parseYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) return null;
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (Exception e) {
            return null;
        }
    }

    public MovieResponse importMovie(String externalId) {
        Optional<Movie> existing = movieRepository.findByExternalId(externalId);
        if (existing.isPresent()) {
            Movie m = existing.get();
            return toResponse(m);
        }

        Map details = externalMovieApiClient.getMovieDetails(externalId);

        Movie movie = new Movie();
        movie.setExternalId(externalId);
        movie.setTitle((String) details.getOrDefault("title", ""));
        movie.setOverview((String) details.getOrDefault("overview", ""));

        String releaseDate = (String) details.get("release_date");
        movie.setYear(parseYear(releaseDate));

        Object runtimeObj = details.get("runtime");
        if (runtimeObj instanceof Number n) {
            movie.setRuntimeMinutes(n.intValue());
        }

        String posterPath = (String) details.get("poster_path");
        movie.setPosterUrl(posterPath == null ? null : "https://image.tmdb.org/t/p/w500" + posterPath);

        Movie saved = saveIfNotExists(movie);
        return toResponse(saved);
    }

    public MovieResponse getMovieResponse(Long id) {
        Movie m = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
        return toResponse(m);
    }

    @Transactional
    public Movie importMovieEntity(String externalId) {
        return movieRepository.findByExternalId(externalId)
                .orElseGet(() -> {
                    Map details = externalMovieApiClient.getMovieDetails(externalId);

                    Movie movie = new Movie();
                    movie.setExternalId(externalId);
                    movie.setTitle((String) details.getOrDefault("title", ""));
                    movie.setOverview((String) details.getOrDefault("overview", ""));

                    String releaseDate = (String) details.get("release_date");
                    movie.setYear(parseYear(releaseDate));

                    Object runtimeObj = details.get("runtime");
                    if (runtimeObj instanceof Number n) {
                        movie.setRuntimeMinutes(n.intValue());
                    }

                    String posterPath = (String) details.get("poster_path");
                    movie.setPosterUrl(posterPath == null ? null : "https://image.tmdb.org/t/p/w500" + posterPath);

                    return saveIfNotExists(movie);
                });
    }


    private MovieResponse toResponse(Movie m) {
        return new MovieResponse(
                m.getId(),
                m.getExternalId(),
                m.getTitle(),
                m.getYear(),
                m.getRuntimeMinutes(),
                m.getPosterUrl(),
                m.getOverview()
        );
    }
}
