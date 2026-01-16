package ru.mawshu.movietracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.mawshu.movietracker.domain.Movie;
import ru.mawshu.movietracker.domain.User;
import ru.mawshu.movietracker.domain.UserMovie;
import ru.mawshu.movietracker.domain.WatchStatus;
import ru.mawshu.movietracker.dto.UserMovieResponse;
import ru.mawshu.movietracker.exception.NotFoundException;
import ru.mawshu.movietracker.repository.MovieRepository;
import ru.mawshu.movietracker.repository.UserMovieRepository;
import ru.mawshu.movietracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLibraryServiceTest {

    @Mock UserRepository userRepository;
    @Mock MovieRepository movieRepository;
    @Mock UserMovieRepository userMovieRepository;
    @Mock MovieCatalogService movieCatalogService;

    UserLibraryService service;

    @BeforeEach
    void setUp() {
        service = new UserLibraryService(userRepository, movieRepository, userMovieRepository, movieCatalogService);
    }

    // ----- addOrUpdateUserMovie -----

    @Test
    void addOrUpdateUserMovie_whenUserNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.addOrUpdateUserMovie(1L, "10", WatchStatus.PLANNED));

        assertEquals("User not found", ex.getMessage());
        verifyNoInteractions(movieCatalogService);
    }

    @Test
    void addOrUpdateUserMovie_whenMovieMissingAfterImport_throws() {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(movieRepository.findByExternalId("10")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.addOrUpdateUserMovie(1L, "10", WatchStatus.PLANNED));

        assertEquals("Movie not found after import", ex.getMessage());
        verify(movieCatalogService).importMovie("10");
    }

    @Test
    void addOrUpdateUserMovie_whenNewUserMovie_createsAndSaves() {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", 1L);

        Movie m = new Movie();
        ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10");
        m.setTitle("T");

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(movieRepository.findByExternalId("10")).thenReturn(Optional.of(m));
        when(userMovieRepository.findByUserIdAndMovieId(1L, 100L)).thenReturn(Optional.empty());

        when(userMovieRepository.save(any(UserMovie.class))).thenAnswer(inv -> {
            UserMovie um = inv.getArgument(0);
            ReflectionTestUtils.setField(um, "id", 55L);
            return um;
        });

        UserMovieResponse res = service.addOrUpdateUserMovie(1L, "10", WatchStatus.PLANNED);

        assertEquals(55L, res.getId());
        assertEquals(WatchStatus.PLANNED, res.getStatus());
        assertEquals("10", res.getMovie().getExternalId());

        verify(movieCatalogService).importMovie("10");
        verify(userMovieRepository).save(any(UserMovie.class));
    }

    @Test
    void addOrUpdateUserMovie_whenWatched_setsWatchedAt_onlyIfNull() {
        User u = new User(); ReflectionTestUtils.setField(u, "id", 1L);
        Movie m = new Movie(); ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10"); m.setTitle("T");

        UserMovie existing = new UserMovie();
        existing.setWatchedAt(LocalDateTime.now().minusDays(1)); // уже было

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(movieRepository.findByExternalId("10")).thenReturn(Optional.of(m));
        when(userMovieRepository.findByUserIdAndMovieId(1L, 100L)).thenReturn(Optional.of(existing));
        when(userMovieRepository.save(any(UserMovie.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMovieResponse res = service.addOrUpdateUserMovie(1L, "10", WatchStatus.WATCHED);

        // watchedAt НЕ должен обнуляться и не обязан меняться (в сервисе меняется только если null)
        assertNotNull(res.getWatchedAt());
        assertEquals(WatchStatus.WATCHED, res.getStatus());
    }

    @Test
    void addOrUpdateUserMovie_whenPlanned_resetsWatchedAtToNull() {
        User u = new User(); ReflectionTestUtils.setField(u, "id", 1L);
        Movie m = new Movie(); ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10"); m.setTitle("T");

        UserMovie existing = new UserMovie();
        existing.setWatchedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(movieRepository.findByExternalId("10")).thenReturn(Optional.of(m));
        when(userMovieRepository.findByUserIdAndMovieId(1L, 100L)).thenReturn(Optional.of(existing));
        when(userMovieRepository.save(any(UserMovie.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMovieResponse res = service.addOrUpdateUserMovie(1L, "10", WatchStatus.PLANNED);

        assertNull(res.getWatchedAt());
        assertEquals(WatchStatus.PLANNED, res.getStatus());
    }

    // ----- getUserMovies -----

    @Test
    void getUserMovies_whenUserNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getUserMovies(1L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getUserMovies_success_mapsList() {
        User u = new User(); ReflectionTestUtils.setField(u, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        Movie m = new Movie();
        ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10");
        m.setTitle("T");

        UserMovie um = new UserMovie();
        ReflectionTestUtils.setField(um, "id", 55L);
        um.setMovie(m);
        um.setStatus(WatchStatus.PLANNED);
        um.setLiked(false);

        when(userMovieRepository.findByUserId(1L)).thenReturn(List.of(um));

        List<UserMovieResponse> res = service.getUserMovies(1L);

        assertEquals(1, res.size());
        assertEquals(55L, res.get(0).getId());
        assertEquals("10", res.get(0).getMovie().getExternalId());
    }

    // ----- updateStatus / updateRating / updateLiked / delete -----

    @Test
    void updateStatus_whenNotFound_throws() {
        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updateStatus(1L, 10L, WatchStatus.WATCHED));

        assertEquals("UserMovie not found", ex.getMessage());
    }

    @Test
    void updateStatus_whenWatched_setsWatchedAt() {
        UserMovie um = new UserMovie();
        um.setWatchedAt(null);

        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(um));
        when(userMovieRepository.save(any(UserMovie.class))).thenAnswer(inv -> inv.getArgument(0));

        // movie нужен для toResponse
        Movie m = new Movie();
        ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10");
        m.setTitle("T");
        um.setMovie(m);

        UserMovieResponse res = service.updateStatus(1L, 10L, WatchStatus.WATCHED);

        assertEquals(WatchStatus.WATCHED, res.getStatus());
        assertNotNull(res.getWatchedAt());
    }

    @Test
    void updateStatus_whenPlanned_resetsWatchedAt() {
        UserMovie um = new UserMovie();
        um.setWatchedAt(LocalDateTime.now());

        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(um));
        when(userMovieRepository.save(any(UserMovie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie m = new Movie();
        ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10");
        m.setTitle("T");
        um.setMovie(m);

        UserMovieResponse res = service.updateStatus(1L, 10L, WatchStatus.PLANNED);

        assertEquals(WatchStatus.PLANNED, res.getStatus());
        assertNull(res.getWatchedAt());
    }

    @Test
    void updateRating_success_setsRating_andSaves() {
        UserMovie um = new UserMovie();
        Movie m = new Movie();
        ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10");
        m.setTitle("T");
        um.setMovie(m);

        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(um));
        when(userMovieRepository.save(any(UserMovie.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMovieResponse res = service.updateRating(1L, 10L, 8);

        assertEquals(8, res.getRating());
        ArgumentCaptor<UserMovie> captor = ArgumentCaptor.forClass(UserMovie.class);
        verify(userMovieRepository).save(captor.capture());
        assertEquals(8, captor.getValue().getRating());
    }

    @Test
    void updateLiked_whenNotFound_throws() {
        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updateLiked(1L, 10L, true));

        assertEquals("UserMovie not found", ex.getMessage());
    }

    @Test
    void updateLiked_success_setsLiked_andSaves() {
        UserMovie um = new UserMovie();
        Movie m = new Movie();
        ReflectionTestUtils.setField(m, "id", 100L);
        m.setExternalId("10");
        m.setTitle("T");
        um.setMovie(m);

        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(um));
        when(userMovieRepository.save(any(UserMovie.class))).thenAnswer(inv -> inv.getArgument(0));

        UserMovieResponse res = service.updateLiked(1L, 10L, true);

        assertTrue(res.isLiked());
        verify(userMovieRepository).save(any(UserMovie.class));
    }

    @Test
    void deleteUserMovie_whenFound_deletes() {
        UserMovie um = new UserMovie();
        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(um));

        service.deleteUserMovie(1L, 10L);

        verify(userMovieRepository).delete(um);
    }

    @Test
    void deleteUserMovie_whenNotFound_throws() {
        when(userMovieRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deleteUserMovie(1L, 10L));

        assertEquals("UserMovie not found", ex.getMessage());
    }
}
