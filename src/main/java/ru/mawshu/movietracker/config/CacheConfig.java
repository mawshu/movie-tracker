package ru.mawshu.movietracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.movie-search.ttl-minutes}") long ttlMinutes,
            @Value("${app.cache.movie-search.max-size}") long maxSize
    ) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("movieSearch");
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                        .maximumSize(maxSize)
        );
        return cacheManager;
    }
}