package ru.mawshu.movietracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mawshu.movietracker.TestMocksConfig;
import ru.mawshu.movietracker.dto.CreateWatchlistRequest;
import ru.mawshu.movietracker.dto.ReorderWatchlistRequest;
import ru.mawshu.movietracker.dto.WatchlistResponse;
import ru.mawshu.movietracker.service.WatchlistService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WatchlistController.class)
@Import(TestMocksConfig.class)
class WatchlistControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired WatchlistService watchlistService; // mock из TestMocksConfig

    @Test
    void createWatchlist_returns201() throws Exception {
        CreateWatchlistRequest req = new CreateWatchlistRequest();
        req.setTitle("title");
        req.setDescription("desc");

        when(watchlistService.createWatchlist(eq(1L), eq("title"), eq("desc")))
                .thenReturn(new WatchlistResponse(
                        10L, 1L, "title", "desc", LocalDateTime.now(), List.of()
                ));

        mockMvc.perform(post("/api/users/1/watchlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void reorderItems_invalid_returns400() throws Exception {
        ReorderWatchlistRequest req = new ReorderWatchlistRequest();
        req.setOrderedItemIds(null); // @NotNull

        mockMvc.perform(patch("/api/watchlists/1/items/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteWatchlist_returns204() throws Exception {
        doNothing().when(watchlistService).deleteWatchlist(1L);

        mockMvc.perform(delete("/api/watchlists/1"))
                .andExpect(status().isNoContent());
    }
}
