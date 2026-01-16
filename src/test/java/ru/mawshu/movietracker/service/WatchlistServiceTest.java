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
import ru.mawshu.movietracker.domain.Watchlist;
import ru.mawshu.movietracker.domain.WatchlistItem;
import ru.mawshu.movietracker.dto.WatchlistItemResponse;
import ru.mawshu.movietracker.dto.WatchlistResponse;
import ru.mawshu.movietracker.exception.ConflictException;
import ru.mawshu.movietracker.exception.NotFoundException;
import ru.mawshu.movietracker.repository.UserRepository;
import ru.mawshu.movietracker.repository.WatchlistItemRepository;
import ru.mawshu.movietracker.repository.WatchlistRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock WatchlistRepository watchlistRepository;
    @Mock WatchlistItemRepository watchlistItemRepository;
    @Mock UserRepository userRepository;
    @Mock MovieCatalogService movieCatalogService;

    WatchlistService service;

    @BeforeEach
    void setUp() {
        service = new WatchlistService(watchlistRepository, watchlistItemRepository, userRepository, movieCatalogService);
    }

    // ---------------- createWatchlist ----------------

    @Test
    void createWatchlist_whenUserNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.createWatchlist(1L, "t", "d"));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void createWatchlist_success_setsFields_andSaves() {
        User u = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(inv -> {
            Watchlist w = inv.getArgument(0);
            setId(w, 10L);
            return w;
        });

        WatchlistResponse res = service.createWatchlist(1L, "My list", "Desc");

        assertEquals(10L, res.getId());
        assertEquals(1L, res.getUserId());
        assertEquals("My list", res.getTitle());
        assertEquals("Desc", res.getDescription());
        assertNotNull(res.getCreatedAt());

        ArgumentCaptor<Watchlist> captor = ArgumentCaptor.forClass(Watchlist.class);
        verify(watchlistRepository).save(captor.capture());
        assertSame(u, captor.getValue().getUser());
        assertNotNull(captor.getValue().getCreatedAt());
    }

    // ---------------- getUserWatchlists ----------------

    @Test
    void getUserWatchlists_whenUserDoesNotExist_throws() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getUserWatchlists(1L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getUserWatchlists_success_sortsItemsByPosition() {
        when(userRepository.existsById(1L)).thenReturn(true);

        User u = user(1L);
        Movie m = movie(100L, "ext", "Film");

        WatchlistItem itPos2 = item(501L, 2, m);
        WatchlistItem itPos1 = item(502L, 1, m);

        Watchlist w = watchlist(10L, u, "L", "D");
        w.getItems().add(itPos2);
        w.getItems().add(itPos1);

        when(watchlistRepository.findByUserId(1L)).thenReturn(List.of(w));

        List<WatchlistResponse> res = service.getUserWatchlists(1L);

        assertEquals(1, res.size());
        assertEquals(2, res.get(0).getItems().size());
        assertEquals(1, res.get(0).getItems().get(0).getPosition());
        assertEquals(2, res.get(0).getItems().get(1).getPosition());
    }

    // ---------------- addMovieToWatchlist ----------------

    @Test
    void addMovieToWatchlist_whenWatchlistNotFound_throws() {
        when(watchlistRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.addMovieToWatchlist(10L, "x"));

        assertEquals("Watchlist not found", ex.getMessage());
        verifyNoInteractions(movieCatalogService);
    }

    @Test
    void addMovieToWatchlist_whenDuplicate_throwsConflict() {
        Watchlist w = watchlist(10L, user(1L), "t", "d");
        Movie m = movie(100L, "x", "Film");

        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(w));
        when(movieCatalogService.importMovieEntity("x")).thenReturn(m);
        when(watchlistItemRepository.existsByWatchlistIdAndMovieId(10L, 100L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.addMovieToWatchlist(10L, "x"));

        assertEquals("Movie already exists in this watchlist", ex.getMessage());
    }

    @Test
    void addMovieToWatchlist_whenMaxPositionNull_setsPosition1_andSetsAddedAt() {
        Watchlist w = watchlist(10L, user(1L), "t", "d");
        Movie m = movie(100L, "x", "Film");

        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(w));
        when(movieCatalogService.importMovieEntity("x")).thenReturn(m);
        when(watchlistItemRepository.existsByWatchlistIdAndMovieId(10L, 100L)).thenReturn(false);
        when(watchlistItemRepository.findMaxPosition(10L)).thenReturn(null);

        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenAnswer(inv -> {
            WatchlistItem it = inv.getArgument(0);
            setId(it, 1L);
            return it;
        });

        WatchlistItemResponse res = service.addMovieToWatchlist(10L, "x");

        assertEquals(1, res.getPosition());
        assertNotNull(res.getAddedAt());

        ArgumentCaptor<WatchlistItem> captor = ArgumentCaptor.forClass(WatchlistItem.class);
        verify(watchlistItemRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getPosition());
        assertNotNull(captor.getValue().getAddedAt());
        assertSame(m, captor.getValue().getMovie());
        assertSame(w, captor.getValue().getWatchlist());
    }

    @Test
    void addMovieToWatchlist_whenMaxPosition7_setsPosition8() {
        Watchlist w = watchlist(10L, user(1L), "t", "d");
        Movie m = movie(100L, "x", "Film");

        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(w));
        when(movieCatalogService.importMovieEntity("x")).thenReturn(m);
        when(watchlistItemRepository.existsByWatchlistIdAndMovieId(10L, 100L)).thenReturn(false);
        when(watchlistItemRepository.findMaxPosition(10L)).thenReturn(7);
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenAnswer(inv -> inv.getArgument(0));

        WatchlistItemResponse res = service.addMovieToWatchlist(10L, "x");
        assertEquals(8, res.getPosition());
    }

    // ---------------- removeWatchlistItem / deleteWatchlist ----------------

    @Test
    void removeWatchlistItem_whenNotFound_throws() {
        when(watchlistItemRepository.findByIdAndWatchlistId(5L, 10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.removeWatchlistItem(10L, 5L));

        assertEquals("Watchlist item not found", ex.getMessage());
    }

    @Test
    void removeWatchlistItem_success_deletes() {
        WatchlistItem item = new WatchlistItem();
        setId(item, 5L);

        when(watchlistItemRepository.findByIdAndWatchlistId(5L, 10L)).thenReturn(Optional.of(item));

        service.removeWatchlistItem(10L, 5L);

        verify(watchlistItemRepository).delete(item);
    }

    @Test
    void deleteWatchlist_whenNotFound_throws() {
        when(watchlistRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deleteWatchlist(10L));

        assertEquals("Watchlist not found", ex.getMessage());
    }

    @Test
    void deleteWatchlist_success_deletes() {
        Watchlist w = watchlist(10L, user(1L), "t", "d");
        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(w));

        service.deleteWatchlist(10L);

        verify(watchlistRepository).delete(w);
    }

    // ---------------- reorderItems ----------------

    @Test
    void reorderItems_whenWatchlistNotFound_throws() {
        when(watchlistRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.reorderItems(10L, List.of(1L)));

        assertEquals("Watchlist not found", ex.getMessage());
    }

    @Test
    void reorderItems_whenItemNotFound_throwsWithIdInMessage() {
        Watchlist w = watchlist(10L, user(1L), "t", "d");

        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(w));
        when(watchlistItemRepository.findByIdAndWatchlistId(1L, 10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.reorderItems(10L, List.of(1L)));

        assertEquals("Watchlist item not found: 1", ex.getMessage());
    }

    @Test
    void reorderItems_success_updatesPositionsInOrder() {
        Watchlist w = watchlist(10L, user(1L), "t", "d");

        WatchlistItem i1 = new WatchlistItem(); setId(i1, 1L); i1.setPosition(99);
        WatchlistItem i2 = new WatchlistItem(); setId(i2, 2L); i2.setPosition(99);

        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(w));
        when(watchlistItemRepository.findByIdAndWatchlistId(1L, 10L)).thenReturn(Optional.of(i1));
        when(watchlistItemRepository.findByIdAndWatchlistId(2L, 10L)).thenReturn(Optional.of(i2));

        service.reorderItems(10L, List.of(2L, 1L));

        assertEquals(2, i1.getPosition());
        assertEquals(1, i2.getPosition());
    }

    // ---------------- getWatchlist ----------------

    @Test
    void getWatchlist_whenNotFound_throws() {
        when(watchlistRepository.findByIdWithItems(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getWatchlist(10L));
        assertEquals("Watchlist not found", ex.getMessage());
    }

    @Test
    void getWatchlist_success_sortsItemsAndMapsMovie() {
        User u = user(1L);

        Movie m = movie(100L, "ext", "Film");
        m.setRuntimeMinutes(90);
        m.setPosterUrl("p");
        m.setOverview("o");

        WatchlistItem itPos2 = item(501L, 2, m);
        WatchlistItem itPos1 = item(502L, 1, m);

        Watchlist w = watchlist(10L, u, "L", "D");
        w.getItems().add(itPos2);
        w.getItems().add(itPos1);

        when(watchlistRepository.findByIdWithItems(10L)).thenReturn(Optional.of(w));

        WatchlistResponse res = service.getWatchlist(10L);

        assertEquals(10L, res.getId());
        assertEquals(1L, res.getUserId());
        assertEquals(2, res.getItems().size());
        assertEquals(1, res.getItems().get(0).getPosition());
        assertEquals(2, res.getItems().get(1).getPosition());

        assertEquals(100L, res.getItems().get(0).getMovie().getId());
        assertEquals("ext", res.getItems().get(0).getMovie().getExternalId());
        assertEquals("Film", res.getItems().get(0).getMovie().getTitle());
    }

    // ---------------- helpers ----------------

    private static void setId(Object target, long id) {
        ReflectionTestUtils.setField(target, "id", id);
    }

    private static User user(long id) {
        User u = new User();
        setId(u, id);
        return u;
    }

    private static Movie movie(long id, String externalId, String title) {
        Movie m = new Movie();
        setId(m, id);
        m.setExternalId(externalId);
        m.setTitle(title);
        m.setYear(2020);
        return m;
    }

    private static Watchlist watchlist(long id, User user, String title, String description) {
        Watchlist w = new Watchlist();
        setId(w, id);
        w.setUser(user);
        w.setTitle(title);
        w.setDescription(description);
        w.setCreatedAt(LocalDateTime.now());
        return w;
    }

    private static WatchlistItem item(long id, int position, Movie movie) {
        WatchlistItem it = new WatchlistItem();
        setId(it, id);
        it.setPosition(position);
        it.setAddedAt(LocalDateTime.now());
        it.setMovie(movie);
        return it;
    }
}
