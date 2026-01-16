package ru.mawshu.movietracker.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_items",
        uniqueConstraints = @UniqueConstraint(name = "uq_watchlist_movie", columnNames = {"watchlist_id", "movie_id"}))
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "watchlist_id", nullable = false)
    private Watchlist watchlist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public WatchlistItem() {}

    public Long getId() { return id; }
    public Watchlist getWatchlist() { return watchlist; }
    public void setWatchlist(Watchlist watchlist) { this.watchlist = watchlist; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
