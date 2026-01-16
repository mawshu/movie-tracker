package ru.mawshu.movietracker.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateLikedRequest {
    @NotNull
    private Boolean liked;

    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
}
