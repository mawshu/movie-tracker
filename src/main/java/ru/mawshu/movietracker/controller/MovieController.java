package ru.mawshu.movietracker.controller;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import ru.mawshu.movietracker.dto.MovieSearchRequest;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import ru.mawshu.movietracker.dto.MovieSearchItem;
import ru.mawshu.movietracker.dto.MovieResponse;
import ru.mawshu.movietracker.service.MovieCatalogService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieCatalogService movieCatalogService;

    public MovieController(MovieCatalogService movieCatalogService) {
        this.movieCatalogService = movieCatalogService;
    }

    @GetMapping("/search")
    public List<MovieSearchItem> searchMovies(MovieSearchRequest request) {
        return movieCatalogService.searchMovies(
                request.getQuery(),
                request.getYear(),
                request.getPage(),
                request.getSize()
        );
    }

    @GetMapping("/{id}")
    public MovieResponse getMovie(@PathVariable Long id) {
        return movieCatalogService.getMovieResponse(id);
    }

    @PostMapping("/import/{externalId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse importMovie(@PathVariable String externalId) {
        return movieCatalogService.importMovie(externalId);
    }
}
