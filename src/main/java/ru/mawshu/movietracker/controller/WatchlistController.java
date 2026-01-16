package ru.mawshu.movietracker.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mawshu.movietracker.dto.*;
import ru.mawshu.movietracker.service.WatchlistService;

import java.util.List;

@RestController
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping("/api/users/{userId}/watchlists")
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistResponse createWatchlist(
            @PathVariable Long userId,
            @Valid @RequestBody CreateWatchlistRequest request
    ) {
        return watchlistService.createWatchlist(userId, request.getTitle(), request.getDescription());
    }

    @GetMapping("/api/users/{userId}/watchlists")
    public List<WatchlistResponse> getUserWatchlists(@PathVariable Long userId) {
        return watchlistService.getUserWatchlists(userId);
    }

    @GetMapping("/api/watchlists/{watchlistId}")
    public WatchlistResponse getWatchlist(@PathVariable Long watchlistId) {
        return watchlistService.getWatchlist(watchlistId);
    }

    @PostMapping("/api/watchlists/{watchlistId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistItemResponse addItem(
            @PathVariable Long watchlistId,
            @Valid @RequestBody AddWatchlistItemRequest request
    ) {
        return watchlistService.addMovieToWatchlist(watchlistId, request.getExternalId());
    }

    @DeleteMapping("/api/watchlists/{watchlistId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long watchlistId, @PathVariable Long itemId) {
        watchlistService.removeWatchlistItem(watchlistId, itemId);
    }

    @DeleteMapping("/api/watchlists/{watchlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWatchlist(@PathVariable Long watchlistId) {
        watchlistService.deleteWatchlist(watchlistId);
    }

    @PatchMapping("/api/watchlists/{watchlistId}/items/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(
            @PathVariable Long watchlistId,
            @Valid @RequestBody ReorderWatchlistRequest request
    ) {
        watchlistService.reorderItems(watchlistId, request.getOrderedItemIds());
    }
}
