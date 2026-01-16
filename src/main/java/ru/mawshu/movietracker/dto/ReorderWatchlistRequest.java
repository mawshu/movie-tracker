package ru.mawshu.movietracker.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReorderWatchlistRequest {
    @NotNull
    private List<Long> orderedItemIds;

    public List<Long> getOrderedItemIds() { return orderedItemIds; }
    public void setOrderedItemIds(List<Long> orderedItemIds) { this.orderedItemIds = orderedItemIds; }
}
