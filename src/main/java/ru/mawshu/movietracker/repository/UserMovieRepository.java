package ru.mawshu.movietracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mawshu.movietracker.domain.UserMovie;

import java.util.List;
import java.util.Optional;

public interface UserMovieRepository extends JpaRepository<UserMovie, Long> {
    List<UserMovie> findByUserId(Long userId);
    Optional<UserMovie> findByIdAndUserId(Long id, Long userId);

    Optional<UserMovie> findByUserIdAndMovieId(Long userId, Long movieId);
}
