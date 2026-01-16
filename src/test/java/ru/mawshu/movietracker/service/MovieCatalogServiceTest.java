package ru.mawshu.movietracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.mawshu.movietracker.domain.Movie;
import ru.mawshu.movietracker.dto.MovieSearchItem;
import ru.mawshu.movietracker.dto.MovieResponse;
import ru.mawshu.movietracker.exception.NotFoundException;
import ru.mawshu.movietracker.integration.ExternalMovieApiClient;
import ru.mawshu.movietracker.repository.MovieRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieCatalogServiceTest {

    @Mock MovieRepository movieRepository;
    @Mock ExternalMovieApiClient externalMovieApiClient;

    MovieCatalogService service;

    @BeforeEach
    void setUp() {
        service = new MovieCatalogService(movieRepository, externalMovieApiClient);
    }

    @Test
    void saveIfNotExists_whenExists_returnsExisting_andDoesNotSave() {
        Movie existing = new Movie();
        existing.setExternalId("123");

        when(movieRepository.findByExternalId("123")).thenReturn(Optional.of(existing));

        Movie result = service.saveIfNotExists(existing);

        assertSame(existing, result);
        verify(movieRepository, never()).save(any());
    }

    @Test
    void saveIfNotExists_whenNotExists_setsCreatedAt_andSaves() {
        Movie input = new Movie();
        input.setExternalId("999");

        when(movieRepository.findByExternalId("999")).thenReturn(Optional.empty());
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie result = service.saveIfNotExists(input);

        assertNotNull(result.getCreatedAt(), "createdAt должен быть выставлен");
        verify(movieRepository).save(input);
    }

    @Test
    void searchMovies_whenResultsMissing_returnsEmptyList() {
        when(externalMovieApiClient.searchMovies("q", 0, 1))
                .thenReturn(Map.of("page", 1));

        List<MovieSearchItem> res = service.searchMovies("q", 0, 1, 10);

        assertTrue(res.isEmpty());
    }

    @Test
    void searchMovies_mapsFields_limitsBySize_andBuildsPosterUrlAndYear() {
        Map<String, Object> row1 = Map.of(
                "id", 101,
                "title", "Film A",
                "release_date", "2020-05-01",
                "overview", "desc",
                "poster_path", "/p1.jpg"
        );
        Map<String, Object> row2 = new java.util.HashMap<>();
        row2.put("id", 202);
        row2.put("title", "Film B");
        row2.put("release_date", "bad");
        row2.put("overview", "");
        row2.put("poster_path", null);

        when(externalMovieApiClient.searchMovies("q", 2020, 1))
                .thenReturn(Map.of("results", List.of(row1, row2)));

        List<MovieSearchItem> res = service.searchMovies("q", 2020, 1, 1);

        assertEquals(1, res.size());
        MovieSearchItem item = res.get(0);

        assertEquals("101", item.getExternalId());
        assertEquals("Film A", item.getTitle());
        assertEquals(2020, item.getYear());
        assertEquals("https://image.tmdb.org/t/p/w500/p1.jpg", item.getPosterUrl());
        assertEquals("desc", item.getOverview());
    }

    @Test
    void importMovie_whenExists_doesNotCallExternalApi() {
        Movie existing = new Movie();
        ReflectionTestUtils.setField(existing, "id", 1L);
        existing.setExternalId("777");
        existing.setTitle("X");

        when(movieRepository.findByExternalId("777")).thenReturn(Optional.of(existing));

        MovieResponse res = service.importMovie("777");

        assertEquals(1L, res.getId());
        assertEquals("777", res.getExternalId());
        assertEquals("X", res.getTitle());

        verify(externalMovieApiClient, never()).getMovieDetails(anyString());
    }

    @Test
    void importMovie_whenNotExists_callsExternalApi_savesAndReturnsResponse() {
        when(movieRepository.findByExternalId("555")).thenReturn(Optional.empty());

        when(externalMovieApiClient.getMovieDetails("555"))
                .thenReturn(Map.of(
                        "title", "Imported",
                        "overview", "ov",
                        "release_date", "2019-01-01",
                        "runtime", 123,
                        "poster_path", "/pp.jpg"
                ));

        when(movieRepository.findByExternalId("555")).thenReturn(Optional.empty());

        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> {
            Movie m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "id", 10L);
            return m;
        });

        MovieResponse res = service.importMovie("555");

        assertEquals(10L, res.getId());
        assertEquals("555", res.getExternalId());
        assertEquals("Imported", res.getTitle());
        assertEquals(2019, res.getYear());
        assertEquals(123, res.getRuntimeMinutes());
        assertEquals("https://image.tmdb.org/t/p/w500/pp.jpg", res.getPosterUrl());

        verify(externalMovieApiClient).getMovieDetails("555");

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(captor.capture());
        assertNotNull(captor.getValue().getCreatedAt());
    }

    @Test
    void getMovieResponse_whenNotFound_throwsNotFoundException() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getMovieResponse(999L));
        assertEquals("Movie not found", ex.getMessage());
    }
}
