package com.gnl.workhub.coreservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // 1. Create a clean, default Jackson 3 mapper instance
        JsonMapper baseMapper = JsonMapper.builder().build();

        // 2. Build your fallback global configuration using the non-deprecated generic serializer
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJacksonJsonRedisSerializer(baseMapper)
                ));

        // 3. FIX HERE: Instantiate the modern serializer using its direct constructor
        JacksonJsonRedisSerializer<List> listSerializer = new JacksonJsonRedisSerializer<>(baseMapper, List.class);

        // 4. Build your custom namespace override mapping for "projects" using the listSerializer
        RedisCacheConfiguration projectsConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(listSerializer));

        // 5. Register the typed configuration to the "projects" namespace
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("projects", projectsConfig);

        // 6. Return the fully initialized cache manager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}