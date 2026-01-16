package ru.mawshu.movietracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.mawshu.movietracker.TestMocksConfig;
import ru.mawshu.movietracker.dto.MovieResponse;
import ru.mawshu.movietracker.exception.NotFoundException;
import ru.mawshu.movietracker.service.MovieCatalogService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@Import(TestMocksConfig.class)
class MovieControllerTest {

    @Autowired MockMvc mockMvc;

    @Autowired MovieCatalogService movieCatalogService; // mock из TestMocksConfig

    @Test
    void importMovie_returns201() throws Exception {
        MovieResponse resp = new MovieResponse(1L, "10", "Film", 2020, 120, null, null);
        when(movieCatalogService.importMovie("10")).thenReturn(resp);

        mockMvc.perform(post("/api/movies/import/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("10"));
    }

    @Test
    void getMovie_notFound_returns404() throws Exception {
        when(movieCatalogService.getMovieResponse(99L))
                .thenThrow(new NotFoundException("Movie not found"));

        mockMvc.perform(get("/api/movies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchMovies_ok() throws Exception {
        mockMvc.perform(get("/api/movies/search")
                        .param("query", "test")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }
}
