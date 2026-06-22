package uk.gov.hmcts.reform.civil.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaseUserRolesCacheMetrics Tests")
class CaseUserRolesCacheMetricsTest {

    private static final String CASE_ID = "1234567890";
    private static final String USER_ID = "user-xyz-789";

    @Mock
    private TelemetryService telemetryService;

    private CaseUserRolesCacheMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new CaseUserRolesCacheMetrics(telemetryService);
    }

    @Nested
    @DisplayName("recordHit Tests")
    class RecordHitTests {

        @Test
        void shouldRecordCacheHitFromRedis() {
            metrics.recordHit(CASE_ID, "redis");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.hit"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "cacheStore", "redis"
                ))
            );
        }

        @Test
        void shouldRecordCacheHitFromCaffeine() {
            metrics.recordHit(CASE_ID, "caffeine");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.hit"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "cacheStore", "caffeine"
                ))
            );
        }

        @Test
        void shouldIncludeCaseIdInHitEvent() {
            String customCaseId = "999888777";
            metrics.recordHit(customCaseId, "redis");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.hit"),
                eq(Map.of(
                    "caseId", customCaseId,
                    "cacheStore", "redis"
                ))
            );
        }
    }

    @Nested
    @DisplayName("recordMiss Tests")
    class RecordMissTests {

        @Test
        void shouldRecordCacheMiss() {
            metrics.recordMiss(CASE_ID);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.miss"),
                eq(Map.of("caseId", CASE_ID))
            );
        }

        @Test
        void shouldRecordMissForDifferentCaseIds() {
            String caseId1 = "case-1";
            String caseId2 = "case-2";

            metrics.recordMiss(caseId1);
            metrics.recordMiss(caseId2);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.miss"),
                eq(Map.of("caseId", caseId1))
            );
            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.miss"),
                eq(Map.of("caseId", caseId2))
            );
        }
    }

    @Nested
    @DisplayName("recordEviction Tests")
    class RecordEvictionTests {

        @Test
        void shouldRecordEvictionWithReason() {
            metrics.recordEviction(CASE_ID, "mutation");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.eviction"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "reason", "mutation"
                ))
            );
        }

        @Test
        void shouldRecordEvictionWithDifferentReasons() {
            metrics.recordEviction(CASE_ID, "ttl_expired");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.eviction"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "reason", "ttl_expired"
                ))
            );
        }

        @Test
        void shouldIncludeReasonInEvictionEvent() {
            String reason = "manual_clear";
            metrics.recordEviction(CASE_ID, reason);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.eviction"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "reason", reason
                ))
            );
        }
    }

    @Nested
    @DisplayName("recordError Tests")
    class RecordErrorTests {

        @Test
        void shouldRecordErrorWithErrorType() {
            metrics.recordError(CASE_ID, "redis_connection_timeout");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.error"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "errorType", "redis_connection_timeout"
                ))
            );
        }

        @Test
        void shouldRecordDifferentErrorTypes() {
            metrics.recordError(CASE_ID, "serialization_failed");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.error"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "errorType", "serialization_failed"
                ))
            );
        }

        @Test
        void shouldIncludeErrorTypeInErrorEvent() {
            String errorType = "redis_write_RuntimeException";
            metrics.recordError(CASE_ID, errorType);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.error"),
                eq(Map.of(
                    "caseId", CASE_ID,
                    "errorType", errorType
                ))
            );
        }
    }

    @Nested
    @DisplayName("recordDisabled Tests")
    class RecordDisabledTests {

        @Test
        void shouldRecordDisabledEvent() {
            metrics.recordDisabled();

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.disabled"),
                eq(Map.of())
            );
        }

        @Test
        void shouldRecordDisabledWithEmptyPropertiesMap() {
            metrics.recordDisabled();

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.disabled"),
                anyMap()
            );
        }
    }

    @Nested
    @DisplayName("recordNegative Tests")
    class RecordNegativeTests {

        @Test
        void shouldRecordNegativeCacheEntry() {
            metrics.recordNegative(CASE_ID);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.negative"),
                eq(Map.of("caseId", CASE_ID))
            );
        }

        @Test
        void shouldRecordNegativeForDifferentCaseIds() {
            String caseId1 = "negative-case-1";
            String caseId2 = "negative-case-2";

            metrics.recordNegative(caseId1);
            metrics.recordNegative(caseId2);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.negative"),
                eq(Map.of("caseId", caseId1))
            );
            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.negative"),
                eq(Map.of("caseId", caseId2))
            );
        }
    }

    @Nested
    @DisplayName("recordFallback Tests")
    class RecordFallbackTests {

        @Test
        void shouldRecordFallbackToCaffeine() {
            metrics.recordFallback(CASE_ID);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.fallback"),
                eq(Map.of("caseId", CASE_ID))
            );
        }

        @Test
        void shouldRecordFallbackForDifferentCaseIds() {
            String caseId1 = "fallback-case-1";
            String caseId2 = "fallback-case-2";

            metrics.recordFallback(caseId1);
            metrics.recordFallback(caseId2);

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.fallback"),
                eq(Map.of("caseId", caseId1))
            );
            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.fallback"),
                eq(Map.of("caseId", caseId2))
            );
        }
    }

    @Nested
    @DisplayName("Multiple Metric Calls")
    class MultipleMetricCalls {

        @Test
        void shouldTrackMultipleEventsSequentially() {
            metrics.recordHit(CASE_ID, "redis");
            metrics.recordMiss("another-case");
            metrics.recordEviction(CASE_ID, "mutation");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.hit"),
                eq(Map.of("caseId", CASE_ID, "cacheStore", "redis"))
            );
            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.miss"),
                eq(Map.of("caseId", "another-case"))
            );
            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.eviction"),
                eq(Map.of("caseId", CASE_ID, "reason", "mutation"))
            );
        }

        @Test
        void shouldTrackFullCycleOfEvents() {
            metrics.recordMiss(CASE_ID);
            metrics.recordNegative(CASE_ID);
            metrics.recordEviction(CASE_ID, "ttl_expired");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.miss"),
                eq(Map.of("caseId", CASE_ID))
            );
            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.negative"),
                eq(Map.of("caseId", CASE_ID))
            );
            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.eviction"),
                eq(Map.of("caseId", CASE_ID, "reason", "ttl_expired"))
            );
        }
    }
}