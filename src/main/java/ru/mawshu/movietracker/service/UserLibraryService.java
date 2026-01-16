package ru.mawshu.movietracker.service;

import org.springframework.stereotype.Service;
import ru.mawshu.movietracker.domain.*;
import ru.mawshu.movietracker.dto.MovieResponse;
import ru.mawshu.movietracker.dto.UserMovieResponse;
import ru.mawshu.movietracker.exception.NotFoundException;
import ru.mawshu.movietracker.repository.MovieRepository;
import ru.mawshu.movietracker.repository.UserMovieRepository;
import ru.mawshu.movietracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserLibraryService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserMovieRepository userMovieRepository;
    private final MovieCatalogService movieCatalogService;

    public UserLibraryService(UserRepository userRepository,
                              MovieRepository movieRepository,
                              UserMovieRepository userMovieRepository,
                              MovieCatalogService movieCatalogService) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.userMovieRepository = userMovieRepository;
        this.movieCatalogService = movieCatalogService;
    }

    public UserMovieResponse addOrUpdateUserMovie(Long userId, String externalId, WatchStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        movieCatalogService.importMovie(externalId);
        Movie movie = movieRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Movie not found after import"));

        UserMovie userMovie = userMovieRepository.findByUserIdAndMovieId(userId, movie.getId())
                .orElseGet(UserMovie::new);

        userMovie.setUser(user);
        userMovie.setMovie(movie);
        userMovie.setStatus(status);

        if (status == WatchStatus.WATCHED && userMovie.getWatchedAt() == null) {
            userMovie.setWatchedAt(LocalDateTime.now());
        }
        if (status == WatchStatus.PLANNED) {
            userMovie.setWatchedAt(null);
        }

        UserMovie saved = userMovieRepository.save(userMovie);
        return toResponse(saved);
    }

    public List<UserMovieResponse> getUserMovies(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return userMovieRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public UserMovieResponse updateStatus(Long userId, Long userMovieId, WatchStatus status) {
        UserMovie um = userMovieRepository.findByIdAndUserId(userMovieId, userId)
                .orElseThrow(() -> new NotFoundException("UserMovie not found"));

        um.setStatus(status);
        if (status == WatchStatus.WATCHED) um.setWatchedAt(LocalDateTime.now());
        if (status == WatchStatus.PLANNED) um.setWatchedAt(null);

        return toResponse(userMovieRepository.save(um));
    }

    public UserMovieResponse updateRating(Long userId, Long userMovieId, Integer rating) {
        UserMovie um = userMovieRepository.findByIdAndUserId(userMovieId, userId)
                .orElseThrow(() -> new NotFoundException("UserMovie not found"));
        um.setRating(rating);
        return toResponse(userMovieRepository.save(um));
    }

    public UserMovieResponse updateLiked(Long userId, Long userMovieId, boolean liked) {
        UserMovie um = userMovieRepository.findByIdAndUserId(userMovieId, userId)
                .orElseThrow(() -> new NotFoundException("UserMovie not found"));
        um.setLiked(liked);
        return toResponse(userMovieRepository.save(um));
    }

    public void deleteUserMovie(Long userId, Long userMovieId) {
        UserMovie um = userMovieRepository.findByIdAndUserId(userMovieId, userId)
                .orElseThrow(() -> new NotFoundException("UserMovie not found"));
        userMovieRepository.delete(um);
    }

    private UserMovieResponse toResponse(UserMovie um) {
        Movie m = um.getMovie();
        MovieResponse movieDto = new MovieResponse(
                m.getId(), m.getExternalId(), m.getTitle(), m.getYear(),
                m.getRuntimeMinutes(), m.getPosterUrl(), m.getOverview()
        );

        return new UserMovieResponse(
                um.getId(),
                movieDto,
                um.getStatus(),
                um.getRating(),
                um.isLiked(),
                um.getWatchedAt(),
                um.getCreatedAt(),
                um.getUpdatedAt()
        );
    }
}
