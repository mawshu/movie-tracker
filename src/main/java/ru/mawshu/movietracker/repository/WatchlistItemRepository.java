package ru.mawshu.movietracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mawshu.movietracker.domain.WatchlistItem;

import java.util.List;
import java.util.Optional;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    @Query("select max(i.position) from WatchlistItem i where i.watchlist.id = :watchlistId")
    Integer findMaxPosition(@Param("watchlistId") Long watchlistId);

    Optional<WatchlistItem> findByIdAndWatchlistId(Long id, Long watchlistId);

    boolean existsByWatchlistIdAndMovieId(Long watchlistId, Long movieId);
}
