package ru.mawshu.movietracker.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WatchlistResponse {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<WatchlistItemResponse> items;

    public WatchlistResponse(Long id, Long userId, String title, String description, LocalDateTime createdAt, List<WatchlistItemResponse> items) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<WatchlistItemResponse> getItems() { return items; }
}
