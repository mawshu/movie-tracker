package ru.mawshu.movietracker.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_movies",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_movie", columnNames = {"user_id", "movie_id"}))
public class UserMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchStatus status;

    private Integer rating;

    @Column(nullable = false)
    private boolean liked = false;

    private LocalDateTime watchedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public WatchStatus getStatus() { return status; }
    public void setStatus(WatchStatus status) { this.status = status; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public LocalDateTime getWatchedAt() { return watchedAt; }
    public void setWatchedAt(LocalDateTime watchedAt) { this.watchedAt = watchedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
