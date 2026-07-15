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
                eq(Map.of("caseId", CASE_ID, "cacheStore", "redis"))
            );
        }

        @Test
        void shouldRecordCacheHitFromCaffeine() {
            metrics.recordHit(CASE_ID, "caffeine");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.hit"),
                eq(Map.of("caseId", CASE_ID, "cacheStore", "caffeine"))
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
    }

    @Nested
    @DisplayName("recordEviction Tests")
    class RecordEvictionTests {

        @Test
        void shouldRecordEvictionWithReason() {
            metrics.recordEviction(CASE_ID, "mutation");

            verify(telemetryService).trackEvent(
                eq("cache.case_user_roles.eviction"),
                eq(Map.of("caseId", CASE_ID, "reason", "mutation"))
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
                eq(Map.of("caseId", CASE_ID, "errorType", "redis_connection_timeout"))
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
    }
}
