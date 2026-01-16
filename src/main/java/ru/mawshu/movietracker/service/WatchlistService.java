package ru.mawshu.movietracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mawshu.movietracker.domain.Movie;
import ru.mawshu.movietracker.domain.User;
import ru.mawshu.movietracker.domain.Watchlist;
import ru.mawshu.movietracker.domain.WatchlistItem;
import ru.mawshu.movietracker.dto.MovieResponse;
import ru.mawshu.movietracker.dto.WatchlistItemResponse;
import ru.mawshu.movietracker.dto.WatchlistResponse;
import ru.mawshu.movietracker.exception.ConflictException;
import ru.mawshu.movietracker.exception.NotFoundException;
import ru.mawshu.movietracker.repository.UserRepository;
import ru.mawshu.movietracker.repository.WatchlistItemRepository;
import ru.mawshu.movietracker.repository.WatchlistRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final UserRepository userRepository;
    private final MovieCatalogService movieCatalogService;

    public WatchlistService(
            WatchlistRepository watchlistRepository,
            WatchlistItemRepository watchlistItemRepository,
            UserRepository userRepository,
            MovieCatalogService movieCatalogService
    ) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistItemRepository = watchlistItemRepository;
        this.userRepository = userRepository;
        this.movieCatalogService = movieCatalogService;
    }

    @Transactional
    public WatchlistResponse createWatchlist(Long userId, String title, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Watchlist watchlist = new Watchlist();
        watchlist.setUser(user);
        watchlist.setTitle(title);
        watchlist.setDescription(description);
        watchlist.setCreatedAt(LocalDateTime.now());

        Watchlist saved = watchlistRepository.save(watchlist);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> getUserWatchlists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return watchlistRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WatchlistItemResponse addMovieToWatchlist(Long watchlistId, String externalId) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new NotFoundException("Watchlist not found"));

        Movie movie = movieCatalogService.importMovieEntity(externalId);

        if (watchlistItemRepository.existsByWatchlistIdAndMovieId(watchlistId, movie.getId())) {
            throw new ConflictException("Movie already exists in this watchlist");
        }

        Integer maxPos = watchlistItemRepository.findMaxPosition(watchlistId);
        int nextPos = (maxPos == null) ? 1 : (maxPos + 1);

        WatchlistItem item = new WatchlistItem();
        item.setWatchlist(watchlist);
        item.setMovie(movie);
        item.setPosition(nextPos);
        item.setAddedAt(LocalDateTime.now());

        WatchlistItem saved = watchlistItemRepository.save(item);
        return toItemResponse(saved);
    }

    @Transactional
    public void removeWatchlistItem(Long watchlistId, Long itemId) {
        WatchlistItem item = watchlistItemRepository.findByIdAndWatchlistId(itemId, watchlistId)
                .orElseThrow(() -> new NotFoundException("Watchlist item not found"));
        watchlistItemRepository.delete(item);
    }

    @Transactional
    public void deleteWatchlist(Long watchlistId) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new NotFoundException("Watchlist not found"));
        watchlistRepository.delete(watchlist);
    }

    @Transactional
    public void reorderItems(Long watchlistId, List<Long> orderedItemIds) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new NotFoundException("Watchlist not found"));

        int pos = 1;
        for (Long itemId : orderedItemIds) {
            WatchlistItem item = watchlistItemRepository.findByIdAndWatchlistId(itemId, watchlist.getId())
                    .orElseThrow(() -> new NotFoundException("Watchlist item not found: " + itemId));
            item.setPosition(pos++);
        }
    }

    @Transactional(readOnly = true)
    public WatchlistResponse getWatchlist(Long watchlistId) {
        Watchlist w = watchlistRepository.findByIdWithItems(watchlistId)
                .orElseThrow(() -> new NotFoundException("Watchlist not found"));

        return toResponse(w);
    }


    private WatchlistResponse toResponse(Watchlist watchlist) {
        List<WatchlistItemResponse> items = watchlist.getItems() == null ? List.of()
                : watchlist.getItems().stream()
                .sorted(Comparator.comparing(WatchlistItem::getPosition))
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return new WatchlistResponse(
                watchlist.getId(),
                watchlist.getUser().getId(),
                watchlist.getTitle(),
                watchlist.getDescription(),
                watchlist.getCreatedAt(),
                items
        );
    }

    private WatchlistItemResponse toItemResponse(WatchlistItem item) {
        Movie m = item.getMovie();
        MovieResponse movie = new MovieResponse(
                m.getId(),
                m.getExternalId(),
                m.getTitle(),
                m.getYear(),
                m.getRuntimeMinutes(),
                m.getPosterUrl(),
                m.getOverview()
        );

        return new WatchlistItemResponse(
                item.getId(),
                item.getPosition(),
                item.getAddedAt(),
                movie
        );
    }
}
