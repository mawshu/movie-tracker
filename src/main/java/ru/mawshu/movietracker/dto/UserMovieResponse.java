package ru.mawshu.movietracker.dto;

import ru.mawshu.movietracker.domain.WatchStatus;

import java.time.LocalDateTime;

public class UserMovieResponse {
    private Long id;
    private MovieResponse movie;
    private WatchStatus status;
    private Integer rating;
    private boolean liked;
    private LocalDateTime watchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserMovieResponse(Long id, MovieResponse movie, WatchStatus status, Integer rating,
                             boolean liked, LocalDateTime watchedAt,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.movie = movie;
        this.status = status;
        this.rating = rating;
        this.liked = liked;
        this.watchedAt = watchedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public MovieResponse getMovie() { return movie; }
    public WatchStatus getStatus() { return status; }
    public Integer getRating() { return rating; }
    public boolean isLiked() { return liked; }
    public LocalDateTime getWatchedAt() { return watchedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
