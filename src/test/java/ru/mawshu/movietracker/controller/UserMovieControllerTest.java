package ru.mawshu.movietracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mawshu.movietracker.TestMocksConfig;
import ru.mawshu.movietracker.domain.WatchStatus;
import ru.mawshu.movietracker.dto.AddUserMovieRequest;
import ru.mawshu.movietracker.dto.UpdateLikedRequest;
import ru.mawshu.movietracker.dto.UpdateRatingRequest;
import ru.mawshu.movietracker.dto.UserMovieResponse;
import ru.mawshu.movietracker.service.UserLibraryService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserMovieController.class)
@Import(TestMocksConfig.class)
class UserMovieControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserLibraryService userLibraryService; // mock из TestMocksConfig

    @Test
    void addUserMovie_returns201() throws Exception {
        AddUserMovieRequest req = new AddUserMovieRequest();
        req.setExternalId("10");
        req.setStatus(WatchStatus.PLANNED);

        when(userLibraryService.addOrUpdateUserMovie(eq(1L), eq("10"), eq(WatchStatus.PLANNED)))
                .thenReturn(new UserMovieResponse(
                        1L,
                        null,
                        WatchStatus.PLANNED,
                        null,
                        false,
                        null,
                        null,
                        null
                ));

        mockMvc.perform(post("/api/users/1/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateRating_invalid_returns400() throws Exception {
        UpdateRatingRequest req = new UpdateRatingRequest();
        req.setRating(20); // у тебя @Max(10)

        mockMvc.perform(patch("/api/users/1/library/5/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLiked_ok_returns200() throws Exception {
        UpdateLikedRequest req = new UpdateLikedRequest();
        req.setLiked(true);

        when(userLibraryService.updateLiked(eq(1L), eq(5L), eq(true)))
                .thenReturn(new UserMovieResponse(
                        5L,
                        null,
                        null,
                        null,
                        true,
                        null,
                        null,
                        null));

        mockMvc.perform(patch("/api/users/1/library/5/liked")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
