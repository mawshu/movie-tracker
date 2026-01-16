package ru.mawshu.movietracker.dto;

import jakarta.validation.constraints.NotBlank;

public class AddWatchlistItemRequest {
    @NotBlank
    private String externalId;

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
