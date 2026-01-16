package ru.mawshu.movietracker.dto;

import jakarta.validation.constraints.NotNull;
import ru.mawshu.movietracker.domain.WatchStatus;

public class UpdateStatusRequest {
    @NotNull
    private WatchStatus status;
    public WatchStatus getStatus() { return status; }
    public void setStatus(WatchStatus status) { this.status = status; }
}
