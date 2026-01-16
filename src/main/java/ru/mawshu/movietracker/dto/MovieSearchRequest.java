package ru.mawshu.movietracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class MovieSearchRequest {

    @NotBlank
    private String query;
    private Integer year;
    @Min(value = 1)
    private Integer page = 1;

    @Min(value = 1)
    private Integer size = 10;

    public MovieSearchRequest() {}
    public String getQuery() {return query;}
    public void setQuery(String query) {this.query = query;}
    public Integer getYear() {return year;}
    public void setYear(Integer year) {this.year = year;}
    public Integer getPage() {return page;}
    public void setPage(Integer page) {this.page = page;}
    public Integer getSize() {return size;}
    public void setSize(Integer size) {this.size = size;}
}
