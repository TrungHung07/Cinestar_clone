package com.cinestar.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cau hinh Redis cho 2 muc dich:
 * 1. Cache du lieu doc nhieu (danh sach phim, rap, suat chieu...)
 * 2. Khoá ghế (seat hold) khi user dang chon ghe - xem SeatHoldService
 *
 * LUU Y QUAN TRONG ve serializer: phai dung BasicPolymorphicTypeValidator
 * allowlist thay vi LaissezFaire + NON_FINAL toan cuc. Neu khong, Jackson se
 * ghi @class cho cac Java internal collection (ImmutableCollections$ListN,
 * Collections$UnmodifiableRandomAccessList...) ma KHONG the deserialize nguoc
 * lai -> moi request cache HIT deu 500 "SerializationException: Could not read JSON".
 */
@Configuration
public class RedisConfig {

    private ObjectMapper redisMapper() {
        PolymorphicTypeValidator ptv = com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
                .builder()
                // Chi DTO cua du an duoc doc/ghi typed polymorphic
                .allowIfBaseType("com.cinestar.")
                .allowIfSubType("com.cinestar.")
                .allowIfSubType("java.math.")
                .build();

        /*
         * Resolver TUY BIEN: KHONG BAO GIO ghi type id (@class) cho cac class java.*
         * (immutable collections, java.time...). Neu ghi, Jackson se khong the
         * deserialize nguoc lai -> moi request cache HIT deu 500. Voi cac class
         * java.* thi de JSON thuan, Jackson doc theo declared type (List -> ArrayList,
         * LocalDate -> ISO string...). Chi DTO com.cinestar.* mang @class.
         */
        ObjectMapper.DefaultTypeResolverBuilder resolver = new ObjectMapper.DefaultTypeResolverBuilder(
                ObjectMapper.DefaultTyping.NON_FINAL, ptv) {
            @Override
            public boolean useForType(com.fasterxml.jackson.databind.JavaType t) {
                if (t.getRawClass().getName().startsWith("java.")) {
                    return false; // khong ghi type id cho java.*
                }
                return super.useForType(t);
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Ghi ngay thang dang ISO string thay vi mang timestamp
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Bat buoc init() + inclusion truoc khi setDefaultTyping (nhu activateDefaultTyping lam)
        resolver.init(JsonTypeInfo.Id.CLASS, null);
        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(resolver);
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisMapper()));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(redisMapper()));
        template.afterPropertiesSet();
        return template;
    }

    private GenericJackson2JsonRedisSerializer serializer() {
        return new GenericJackson2JsonRedisSerializer(redisMapper());
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer()));

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        // Danh sach phim trang chu - thay doi it
        perCache.put("movies", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // Chi tiet phim - gan nhu tinh
        perCache.put("movie-details", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // Danh sach rap
        perCache.put("cinemas", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // Suat chieu - TTL ngan vi co ghe duoc ban lien tuc
        perCache.put("showtimes", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        perCache.put("combos", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        perCache.put("promotions", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCache)
                .transactionAware()
                .build();
    }
}
