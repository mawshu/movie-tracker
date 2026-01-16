package ru.mawshu.movietracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mawshu.movietracker.domain.Movie;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
}
