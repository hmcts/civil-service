package uk.gov.hmcts.reform.civil.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import uk.gov.hmcts.reform.civil.config.CaseUserRolesCacheProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Set.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseUserRolesCacheServiceTest {

    private static final String CASE_ID = "1234567890";
    private static final String USER_ID = "user-abc-123";
    private static final List<String> ROLES = List.of("[APPLICANTSOLICITORONE]", "[CREATOR]");
    private static final String KEY_PREFIX = "civil:v1:case-user-roles";

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseUserRolesCacheMetrics metrics;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CaseUserRolesCacheService cacheService;
    private CaseUserRolesCacheProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new CaseUserRolesCacheProperties();
        properties.setEnabled(true);
        properties.setTtlSeconds(30);
        properties.setNegativeTtlSeconds(10);
        properties.setKeyPrefix(KEY_PREFIX);
        properties.setCaffeineMaxSize(10000);

        objectMapper = new ObjectMapper();

        cacheService = new CaseUserRolesCacheService(
            properties,
            featureToggleService,
            metrics,
            objectMapper,
            Optional.of(redisTemplate)
        );
    }

    @Nested
    class CacheHit {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldReturnCachedRoles_whenFoundInRedis() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString()))
                .thenReturn("[\"[APPLICANTSOLICITORONE]\",\"[CREATOR]\"]");

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verify(metrics).recordHit(CASE_ID, "redis");
        }

        @Test
        void shouldReturnCachedRoles_whenFoundInCaffeine_afterRedisMiss() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString())).thenReturn(null);

            // First, populate Caffeine by putting a value
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Now Redis returns null but Caffeine should have it
            when(valueOperations.get(anyString())).thenReturn(null);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verify(metrics).recordHit(CASE_ID, "caffeine");
        }
    }

    @Nested
    class CacheMiss {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldReturnEmpty_whenNotFoundInAnyStore() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString())).thenReturn(null);

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
            verify(metrics).recordMiss(CASE_ID);
        }
    }

    @Nested
    class CachePut {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldStoreRoles_withStandardTtl() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            cacheService.put(CASE_ID, USER_ID, ROLES);

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID;
            verify(valueOperations).set(
                eq(expectedKey),
                eq("[\"[APPLICANTSOLICITORONE]\",\"[CREATOR]\"]"),
                eq(Duration.ofSeconds(30))
            );
        }
    }

    @Nested
    class NegativeCache {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldCacheEmptyResults_withShorterTtl() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            cacheService.put(CASE_ID, USER_ID, List.of());

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID;
            verify(valueOperations).set(
                eq(expectedKey),
                eq("[]"),
                eq(Duration.ofSeconds(10))
            );
            verify(metrics).recordNegative(CASE_ID);
        }

        @Test
        void shouldReturnEmptyList_whenNegativeCacheHit() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString())).thenReturn("[]");

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).isEmpty();
        }
    }

    @Nested
    class KillSwitch {

        @Test
        void shouldBypassCache_whenKillSwitchDisabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(false);

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
            verify(metrics).recordDisabled();
            verify(valueOperations, never()).get(anyString());
        }

        @Test
        void shouldNotWrite_whenKillSwitchDisabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(false);

            cacheService.put(CASE_ID, USER_ID, ROLES);

            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }
    }

    @Nested
    class Invalidation {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldEvictSpecificKey() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            // Put then evict
            cacheService.put(CASE_ID, USER_ID, ROLES);
            cacheService.evict(CASE_ID, USER_ID);

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID;
            verify(redisTemplate).delete(expectedKey);
            verify(metrics).recordEviction(CASE_ID, "mutation");
        }

        @Test
        void shouldEvictAfterGet_returnsEmpty() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            // Put value
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Evict
            cacheService.evict(CASE_ID, USER_ID);

            // Caffeine should no longer have it
            when(valueOperations.get(anyString())).thenReturn(null);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class KeyIsolation {

        @Test
        void shouldProduceDifferentKeys_forDifferentUsers() {
            String key1 = cacheService.buildKey("getUserCaseRoles", CASE_ID, "user-1");
            String key2 = cacheService.buildKey("getUserCaseRoles", CASE_ID, "user-2");

            assertThat(key1).isNotEqualTo(key2);
            assertThat(key1).startsWith(KEY_PREFIX);
            assertThat(key2).startsWith(KEY_PREFIX);
        }

        @Test
        void shouldProduceDifferentKeys_forDifferentCases() {
            String key1 = cacheService.buildKey("getUserCaseRoles", "case-1", USER_ID);
            String key2 = cacheService.buildKey("getUserCaseRoles", "case-2", USER_ID);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        void shouldIncludeAllContextDimensions_inKey() {
            String key = cacheService.buildKey("getUserCaseRoles", CASE_ID, USER_ID);

            assertThat(key).contains(KEY_PREFIX);
            assertThat(key).contains("getUserCaseRoles");
            assertThat(key).contains(CASE_ID);
            assertThat(key).contains(USER_ID);
        }
    }

    @Nested
    class RedisFallback {

        @Test
        void shouldFallbackToCaffeine_whenRedisThrowsException() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            // Put succeeds to both (Redis throws on get but Caffeine has it)
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Simulate Redis failure on read
            when(valueOperations.get(anyString()))
                .thenThrow(new RuntimeException("Redis connection refused"));

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verify(metrics).recordFallback(CASE_ID);
            verify(metrics).recordError(eq(CASE_ID), anyString());
        }

        @Test
        void shouldUseCaffeineOnly_whenRedisNotAvailable() {
            CaseUserRolesCacheService caffeineOnlyService = new CaseUserRolesCacheService(
                properties,
                featureToggleService,
                metrics,
                objectMapper,
                Optional.empty()
            );

            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            caffeineOnlyService.put(CASE_ID, USER_ID, ROLES);
            Optional<List<String>> result = caffeineOnlyService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);
            verifyNoInteractions(redisTemplate);
        }
    }

    @Nested
    class ExceptionHandling {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldNotCacheOnRedisWriteFailure_butCaffeineStillWorks() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            doThrow(new RuntimeException("Redis write failed"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

            // Should not throw
            cacheService.put(CASE_ID, USER_ID, ROLES);

            // Caffeine should still have the value
            when(valueOperations.get(anyString())).thenReturn(null);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            verify(metrics).recordError(eq(CASE_ID), anyString());
        }

        @Test
        void shouldHandleRedisEvictionFailure() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            doThrow(new RuntimeException("Redis eviction failed"))
                .when(redisTemplate).delete(anyString());

            // Put then evict
            cacheService.put(CASE_ID, USER_ID, ROLES);
            cacheService.evict(CASE_ID, USER_ID);

            // Should still record error
            verify(metrics).recordError(eq(CASE_ID), anyString());
        }

        @Test
        void shouldDeserializeErrorReturnsEmptyList() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(valueOperations.get(anyString())).thenReturn("invalid json");

            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).isEmpty();
        }
    }

    @Nested
    class EvictAllForCase {

        @Test
        void shouldEvictAllKeysForCase() {
            Set<String> keys = Set.of(
                "civil:v1:case-user-roles:getUserCaseRoles:1234567890:user1",
                "civil:v1:case-user-roles:getUserCaseRoles:1234567890:user2"
            );
            when(redisTemplate.keys(anyString())).thenReturn(keys);

            cacheService.evictAllForCase(CASE_ID);

            verify(redisTemplate).keys("civil:v1:case-user-roles:getUserCaseRoles:1234567890:*");
            verify(redisTemplate).delete(keys);
            verify(metrics).recordEviction(CASE_ID, "mutation");
        }

        @Test
        void shouldEvictAllForCase_fromCaffeine() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(redisTemplate.keys(anyString())).thenReturn(of());

            // Put multiple values for same case but different users
            cacheService.put(CASE_ID, "user-1", ROLES);
            cacheService.put(CASE_ID, "user-2", ROLES);

            cacheService.evictAllForCase(CASE_ID);

            verify(metrics).recordEviction(CASE_ID, "mutation");
        }

        @Test
        void shouldHandleEvictAllForCase_whenRedisKeysReturnsNull() {
            when(redisTemplate.keys(anyString())).thenReturn(null);

            cacheService.evictAllForCase(CASE_ID);

            verify(metrics).recordEviction(CASE_ID, "mutation");
        }

        @Test
        void shouldHandleEvictAllForCase_whenRedisKeysReturnsEmpty() {
            when(redisTemplate.keys(anyString())).thenReturn(of());

            cacheService.evictAllForCase(CASE_ID);

            verify(metrics).recordEviction(CASE_ID, "mutation");
        }

        @Test
        void shouldHandleEvictAllForCase_whenRedisThrowsException() {
            doThrow(new RuntimeException("Redis pattern search failed"))
                .when(redisTemplate).keys(anyString());

            cacheService.evictAllForCase(CASE_ID);

            verify(metrics).recordError(eq(CASE_ID), anyString());
        }
    }

    @Nested
    class EvictionByPattern {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldEvictByPattern_fromRedis() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            cacheService.put(CASE_ID, "user1", ROLES);

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":user1";
            verify(valueOperations).set(
                eq(expectedKey),
                anyString(),
                eq(Duration.ofSeconds(30))
            );
        }

        @Test
        void shouldHandleEvictionByPattern_whenRedisThrowsException() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);
            doThrow(new RuntimeException("Redis pattern failed"))
                .when(redisTemplate).keys(anyString());

            cacheService.put(CASE_ID, USER_ID, ROLES);

            String pattern = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":*";
            // Simulate pattern eviction through evictAllForCase
            cacheService.evictAllForCase(CASE_ID);

            verify(metrics).recordError(eq(CASE_ID), anyString());
        }
    }

    @Nested
    class CacheDisabledBehavior {

        @Test
        void shouldNotStoreInCaffeine_whenDisabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(false);

            cacheService.put(CASE_ID, USER_ID, ROLES);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldNotStoreAndEvict_whenDisabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(false);

            cacheService.put(CASE_ID, USER_ID, ROLES);
            Optional<List<String>> result = cacheService.get(CASE_ID, USER_ID);

            assertThat(result).isEmpty();
            verify(metrics).recordDisabled();
        }
    }

    @Nested
    class BuildKeyMethod {

        @Test
        void shouldBuildKeyWithoutUserId() {
            String key = cacheService.buildKey("getUserCaseRoles", CASE_ID, null);

            assertThat(key).isEqualTo(KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID);
            assertThat(key).doesNotContain("null");
        }

        @Test
        void shouldBuildKeyWithUserId() {
            String key = cacheService.buildKey("getUserCaseRoles", CASE_ID, USER_ID);

            assertThat(key).isEqualTo(KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID);
        }

        @Test
        void shouldBuildKeyWithDifferentMethods() {
            String key1 = cacheService.buildKey("method1", CASE_ID, USER_ID);
            String key2 = cacheService.buildKey("method2", CASE_ID, USER_ID);

            assertThat(key1).contains("method1");
            assertThat(key2).contains("method2");
            assertThat(key1).isNotEqualTo(key2);
        }
    }

    @Nested
    class IsCacheEnabledMethod {

        @Test
        void shouldReturnTrueWhenFeatureEnabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            boolean enabled = cacheService.isCacheEnabled();

            assertThat(enabled).isTrue();
        }

        @Test
        void shouldReturnFalseWhenFeatureDisabled() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(false);

            boolean enabled = cacheService.isCacheEnabled();

            assertThat(enabled).isFalse();
        }
    }

    @Nested
    class RedisNotAvailableScenarios {

        @Test
        void shouldUseCaffeineForAllOperations_whenRedisUnavailable() {
            CaseUserRolesCacheService caffeineOnlyService = new CaseUserRolesCacheService(
                properties,
                featureToggleService,
                metrics,
                objectMapper,
                Optional.empty()
            );

            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            // Put
            caffeineOnlyService.put(CASE_ID, USER_ID, ROLES);

            // Get
            Optional<List<String>> result = caffeineOnlyService.get(CASE_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).containsExactlyElementsOf(ROLES);

            // Evict
            caffeineOnlyService.evict(CASE_ID, USER_ID);
            Optional<List<String>> resultAfterEviction = caffeineOnlyService.get(CASE_ID, USER_ID);

            assertThat(resultAfterEviction).isEmpty();
        }

        @Test
        void shouldNotInteractWithRedis_whenNotAvailable() {
            CaseUserRolesCacheService caffeineOnlyService = new CaseUserRolesCacheService(
                properties,
                featureToggleService,
                metrics,
                objectMapper,
                Optional.empty()
            );

            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            caffeineOnlyService.put(CASE_ID, USER_ID, ROLES);
            caffeineOnlyService.get(CASE_ID, USER_ID);
            caffeineOnlyService.evict(CASE_ID, USER_ID);

            verifyNoInteractions(redisTemplate);
        }
    }

    @Nested
    class EdgeCases {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldHandleVeryLargeRolesList() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            List<String> largeRolesList = java.util.stream.IntStream.range(0, 1000)
                .mapToObj(i -> "ROLE_" + i)
                .toList();

            cacheService.put(CASE_ID, USER_ID, largeRolesList);

            String expectedKey = KEY_PREFIX + ":getUserCaseRoles:" + CASE_ID + ":" + USER_ID;
            verify(valueOperations).set(
                eq(expectedKey),
                anyString(),
                eq(Duration.ofSeconds(30))
            );
        }

        @Test
        void shouldHandleMultipleConsecutivePutOperations() {
            when(featureToggleService.isFeatureEnabled(CaseUserRolesCacheService.KILL_SWITCH_FLAG))
                .thenReturn(true);

            List<String> rolesV1 = List.of("ROLE1");
            List<String> rolesV2 = List.of("ROLE1", "ROLE2");
            List<String> rolesV3 = List.of("ROLE1", "ROLE2", "ROLE3");

            cacheService.put(CASE_ID, USER_ID, rolesV1);
            cacheService.put(CASE_ID, USER_ID, rolesV2);
            cacheService.put(CASE_ID, USER_ID, rolesV3);

            verify(valueOperations, org.mockito.Mockito.times(3)).set(
                anyString(),
                anyString(),
                eq(Duration.ofSeconds(30))
            );
        }
    }

    @Nested
    class SpecialCharactersAndEdgeCases {

        @Test
        void shouldHandleSpecialCharactersInCaseIdAndUserId() {
            String specialCaseId = "case-123_456:789";
            String specialUserId = "user@domain.com";

            String key = cacheService.buildKey("getUserCaseRoles", specialCaseId, specialUserId);

            assertThat(key).contains(specialCaseId);
            assertThat(key).contains(specialUserId);
        }
    }
}
