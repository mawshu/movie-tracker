package ru.mawshu.movietracker.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mawshu.movietracker.dto.*;
import ru.mawshu.movietracker.service.UserLibraryService;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/library")
public class UserMovieController {

    private final UserLibraryService userLibraryService;

    public UserMovieController(UserLibraryService userLibraryService) {
        this.userLibraryService = userLibraryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserMovieResponse add(@PathVariable Long userId, @Valid @RequestBody AddUserMovieRequest request) {
        return userLibraryService.addOrUpdateUserMovie(userId, request.getExternalId(), request.getStatus());
    }

    @GetMapping
    public List<UserMovieResponse> getAll(@PathVariable Long userId) {
        return userLibraryService.getUserMovies(userId);
    }

    @PatchMapping("/{userMovieId}/status")
    public UserMovieResponse updateStatus(@PathVariable Long userId,
                                          @PathVariable Long userMovieId,
                                          @Valid @RequestBody UpdateStatusRequest request) {
        return userLibraryService.updateStatus(userId, userMovieId, request.getStatus());
    }

    @PatchMapping("/{userMovieId}/rating")
    public UserMovieResponse updateRating(@PathVariable Long userId,
                                          @PathVariable Long userMovieId,
                                          @Valid @RequestBody UpdateRatingRequest request) {
        return userLibraryService.updateRating(userId, userMovieId, request.getRating());
    }

    @PatchMapping("/{userMovieId}/liked")
    public UserMovieResponse updateLiked(@PathVariable Long userId,
                                         @PathVariable Long userMovieId,
                                         @Valid @RequestBody UpdateLikedRequest request) {
        return userLibraryService.updateLiked(userId, userMovieId, request.getLiked());
    }

    @DeleteMapping("/{userMovieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long userMovieId) {
        userLibraryService.deleteUserMovie(userId, userMovieId);
    }
}
