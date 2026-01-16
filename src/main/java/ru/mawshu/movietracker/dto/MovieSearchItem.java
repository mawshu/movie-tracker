package ru.mawshu.movietracker.dto;

public class MovieSearchItem {
    private String externalId;
    private String title;
    private Integer year;
    private String posterUrl;
    private String overview;

    public MovieSearchItem(String externalId, String title, Integer year, String posterUrl, String overview) {
        this.externalId = externalId;
        this.title = title;
        this.year = year;
        this.posterUrl = posterUrl;
        this.overview = overview;
    }

    public String getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public Integer getYear() { return year; }
    public String getPosterUrl() { return posterUrl; }
    public String getOverview() { return overview; }
}
