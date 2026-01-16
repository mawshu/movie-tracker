package ru.mawshu.movietracker.dto;

import java.time.LocalDateTime;

public class WatchlistItemResponse {
    private Long id;
    private Integer position;
    private LocalDateTime addedAt;
    private MovieResponse movie;

    public WatchlistItemResponse(Long id, Integer position, LocalDateTime addedAt, MovieResponse movie) {
        this.id = id;
        this.position = position;
        this.addedAt = addedAt;
        this.movie = movie;
    }

    public Long getId() { return id; }
    public Integer getPosition() { return position; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public MovieResponse getMovie() { return movie; }
}
