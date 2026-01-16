package ru.mawshu.movietracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mawshu.movietracker.domain.Watchlist;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUserId(Long userId);

    @Query("""
    select w from Watchlist w
    left join fetch w.items i
    left join fetch i.movie
    where w.id = :id
""")
    Optional<Watchlist> findByIdWithItems(@Param("id") Long id);
}
