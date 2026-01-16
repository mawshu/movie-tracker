package ru.mawshu.movietracker;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.mawshu.movietracker.repository.UserRepository;
import ru.mawshu.movietracker.service.UserLibraryService;
import ru.mawshu.movietracker.service.WatchlistService;
import ru.mawshu.movietracker.service.MovieCatalogService;

@TestConfiguration
public class TestMocksConfig {

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public MovieCatalogService movieCatalogService() {
        return Mockito.mock(MovieCatalogService.class);
    }

    @Bean
    @Primary
    public UserLibraryService userLibraryService() {
        return Mockito.mock(UserLibraryService.class);
    }

    @Bean
    @Primary
    public WatchlistService watchlistService() {
        return Mockito.mock(WatchlistService.class);
    }
}
