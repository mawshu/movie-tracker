package ru.mawshu.movietracker.dto;

public class MovieResponse {
    private Long id;
    private String externalId;
    private String title;
    private Integer year;
    private Integer runtimeMinutes;
    private String posterUrl;
    private String overview;

    public MovieResponse(Long id, String externalId, String title, Integer year,
                         Integer runtimeMinutes, String posterUrl, String overview) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.year = year;
        this.runtimeMinutes = runtimeMinutes;
        this.posterUrl = posterUrl;
        this.overview = overview;
    }

    public Long getId() { return id; }
    public String getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public Integer getYear() { return year; }
    public Integer getRuntimeMinutes() { return runtimeMinutes; }
    public String getPosterUrl() { return posterUrl; }
    public String getOverview() { return overview; }
}
