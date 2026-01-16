package ru.mawshu.movietracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateRatingRequest {
    @NotNull
    @Min(0)
    @Max(10)
    private Integer rating;

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
