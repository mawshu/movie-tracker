package ru.mawshu.movietracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.mawshu.movietracker.domain.WatchStatus;

public class AddUserMovieRequest {
    @NotBlank
    private String externalId;

    @NotNull
    private WatchStatus status;

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public WatchStatus getStatus() { return status; }
    public void setStatus(WatchStatus status) { this.status = status; }
}
