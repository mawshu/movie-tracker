package ru.mawshu.movietracker.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 64)
    private String externalId;

    @Column(nullable = false)
    private String title;

    private Integer year;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @Column(name = "poster_url", length = 1024)
    private String posterUrl;

    @Column(columnDefinition = "text")
    private String overview;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMovie> userMovies = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WatchlistItem> watchlistItems = new ArrayList<>();

    public Movie() {}

    public Long getId() { return id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getRuntimeMinutes() { return runtimeMinutes; }
    public void setRuntimeMinutes(Integer runtimeMinutes) { this.runtimeMinutes = runtimeMinutes; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Genre> getGenres() { return genres; }
}
