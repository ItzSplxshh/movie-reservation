package com.moviereservation.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import java.time.Duration;

/**
 * Configuration class for Redis caching.
 * Enables Spring's annotation-driven caching support and configures
 * the RedisCacheManager with a 60-second time-to-live and JSON serialization.
 * Used by SeatService to cache seat availability data and reduce
 * database load for frequently accessed queries.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configures and creates the Redis cache manager.
     * Sets a default TTL of 60 seconds for all cache entries, after which
     * cached data is automatically evicted and refreshed from the database
     * on the next request. Uses GenericJackson2JsonRedisSerializer to store
     * cached objects as JSON, ensuring compatibility across application restarts.
     *
     * @param connectionFactory the Redis connection factory provided by Spring Boot
     * @return a configured RedisCacheManager instance
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // Cache entries expire after 60 seconds
                .entryTtl(Duration.ofSeconds(60))
                // Serialize cache values as JSON for readability and compatibility
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}