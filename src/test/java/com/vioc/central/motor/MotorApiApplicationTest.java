package com.vioc.central.motor;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;

import jakarta.inject.Inject;
import javax.cache.Caching;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.github.benmanes.caffeine.jcache.CacheProxy;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;
import com.vioc.core.redis.local.EnableLocalRedis;
import com.vioc.pos.vcdb.aces.domain.VehicleToEngineConfigAces;
import com.vioc.pos.vcdb.aces.domain.YearAces;

class MotorApiApplicationTest {

    @Nested
    @AutoConfigureMockMvc
    @SpringBootTest(properties = {
            "vioc.core.cache.implementation=caffeine",
            // disable for this test.  When running in AWS, we'll have this defaulted to true
            "vioc.core.cache.evict-via-data-change-events=false",
            "vioc.core.data-sync.implementation=VIOC_IN_MEMORY",
            "spring.autoconfigure.exclude=org.redisson.spring.starter.RedissonAutoConfigurationV2"
    })
    @ActiveProfiles("not-local") // prevents registering the default "local" profile
    class NonStoreProfile {

        @Inject
        private MockMvc mockMvc;

        /**
         * The default AWS/K8s profile requires standard oauth2 authentication.
         */
        @Test
        void securityConfigured() throws Exception {
            mockMvc.perform(get("/v1/anything"))
                    // Oauth2 should have been autoconfigured
                    .andExpect(status().isUnauthorized());
        }

    }

    @Nested
    @AutoConfigureMockMvc
    @SpringBootTest(properties = "spring.autoconfigure.exclude=org.redisson.spring.starter.RedissonAutoConfigurationV2")
    @ActiveProfiles("store")
    class StoreProfile {

        @Inject
        private MockMvc mockMvc;

        /**
         * The store profile should not require authentication.
         */
        @Test
        void securityConfigured() throws Exception {
            mockMvc.perform(get("/v1/anything"))
                    // not found instead of not-authorized
                    .andExpect(status().isNotFound());
        }

        @Test
        void testCaffeineCacheConfigured() {
            validateCaffeineConfigurationLoaded();
        }

    }

    @Nested
    @AutoConfigureMockMvc
    @SpringBootTest(properties = "spring.autoconfigure.exclude=org.redisson.spring.starter.RedissonAutoConfigurationV2")
    @ActiveProfiles("local")
    class LocalProfile {

        @Inject
        private MockMvc mockMvc;

        /**
         * The store profile should not require authentication.
         */
        @Test
        void securityConfigured() throws Exception {
            mockMvc.perform(get("/v1/anything"))
                    // not found instead of not-authorized
                    .andExpect(status().isNotFound());
        }

        @Test
        void testCaffeineCacheConfigured() {
            validateCaffeineConfigurationLoaded();
        }

    }

    private void validateCaffeineConfigurationLoaded() {
        //
        // verify that the values match what is in application.conf
        //

        // standard config
        assertThat(getConfiguration(VehicleToEngineConfigAces.class).getMaximumSize())
                .as("has the default maximum size")
                .hasValue(100);
        assertThat(getConfiguration(VehicleToEngineConfigAces.class).getExpireAfterAccess())
                .as("has the default expiry configuration")
                .hasValue(Duration.ofMinutes(5).toNanos());

        // custom config
        assertThat(getConfiguration(YearAces.class).getExpireAfterAccess())
                .as("has the custom eternal configuration")
                .isEmpty();
        assertThat(getConfiguration(YearAces.class).getMaximumSize())
                .as("has the default maximum size, since not overwritten")
                .hasValue(100);
    }

    @SuppressWarnings("resource") // just accessing cache, not creating
    private CaffeineConfiguration<?, ?> getConfiguration(Class<?> entityClass) {
        final var cacheManager = Caching.getCachingProvider(CaffeineCachingProvider.class.getTypeName())
                .getCacheManager();
        final var cacheProxy = cacheManager.getCache(entityClass.getTypeName()).unwrap(CacheProxy.class);
        @SuppressWarnings("unchecked")
        final var config = (CaffeineConfiguration<?, ?>) cacheProxy.getConfiguration(CaffeineConfiguration.class);
        return config;
    }

}
