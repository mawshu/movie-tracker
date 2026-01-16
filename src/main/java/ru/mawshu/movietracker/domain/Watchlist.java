package ru.mawshu.movietracker.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "watchlists")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<WatchlistItem> items = new ArrayList<>();

    public Watchlist() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<WatchlistItem> getItems() { return items; }
}
