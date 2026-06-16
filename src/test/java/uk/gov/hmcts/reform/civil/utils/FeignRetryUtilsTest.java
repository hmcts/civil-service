package uk.gov.hmcts.reform.civil.utils;

import feign.Response;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeignRetryUtilsTest {

    @ParameterizedTest
    @CsvSource({
        "429, 0, true, true",
        "429, 100, false, true",
        "429, 0, false, false",
        "503, 0, true, true",
        "503, 100, false, true",
        "503, 0, false, false",
        "408, 0, true, true",
        "408, 0, false, false",
        "502, 0, true, true",
        "502, 0, false, false",
        "504, 0, true, true",
        "504, 0, false, false",
        "400, 100, true, false",
        "500, 100, true, false"
    })
    void shouldReturnCorrectRetryableStatus(int status, long retryAfter, boolean idempotent, boolean expected) {
        assertThat(FeignRetryUtils.isRetryable(status, retryAfter, idempotent)).isEqualTo(expected);
    }

    @Test
    void shouldReturnTrueForRetryableNonIdempotentMethod() {
        assertThat(FeignRetryUtils.isRetryableNonIdempotentMethod("CoreCaseDataApi#searchCases(String,String,String,String)"))
            .isTrue();
    }

    @Test
    void shouldReturnFalseForOtherMethods() {
        assertThat(FeignRetryUtils.isRetryableNonIdempotentMethod("OtherApi#method()"))
            .isFalse();
        assertThat(FeignRetryUtils.isRetryableNonIdempotentMethod(null))
            .isFalse();
    }

    @Nested
    class GetRetryAfter {
        @Test
        void shouldReturnZeroWhenResponseIsNull() {
            assertThat(FeignRetryUtils.getRetryAfter(null)).isZero();
        }

        @Test
        void shouldReturnZeroWhenHeadersAreNull() {
            Response response = mock(Response.class);
            when(response.headers()).thenReturn(null);
            assertThat(FeignRetryUtils.getRetryAfter(response)).isZero();
        }

        @Test
        void shouldReturnZeroWhenRetryAfterHeaderIsMissing() {
            Response response = mock(Response.class);
            when(response.headers()).thenReturn(Collections.emptyMap());
            assertThat(FeignRetryUtils.getRetryAfter(response)).isZero();
        }

        @Test
        void shouldReturnRetryAfterValueInSeconds() {
            Response response = mock(Response.class);
            Map<String, Collection<String>> headers = Map.of(FeignRetryUtils.RETRY_AFTER, Collections.singletonList("30"));
            when(response.headers()).thenReturn(headers);

            long result = FeignRetryUtils.getRetryAfter(response);
            // It should be roughly currentTime + 30s
            assertThat(result).isGreaterThan(System.currentTimeMillis());
        }

        @Test
        void shouldReturnRetryAfterValueInDateTime() {
            Response response = mock(Response.class);
            String dateStr = "Fri, 31 Dec 2021 23:59:59 GMT";
            Map<String, Collection<String>> headers = Map.of(FeignRetryUtils.RETRY_AFTER, Collections.singletonList(dateStr));
            when(response.headers()).thenReturn(headers);

            long expectedEpoch = ZonedDateTime.parse(dateStr, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
            assertThat(FeignRetryUtils.getRetryAfter(response)).isEqualTo(expectedEpoch);
        }

        @Test
        void shouldReturnFirstValueWhenMultipleHeadersPresent() {
            Response response = mock(Response.class);
            Map<String, Collection<String>> headers = Map.of(FeignRetryUtils.RETRY_AFTER, List.of("30", "60"));
            when(response.headers()).thenReturn(headers);

            long result = FeignRetryUtils.getRetryAfter(response);
            assertThat(result).isGreaterThan(System.currentTimeMillis());
        }
    }

    @Nested
    class RetryAfterDecoderTest {
        @Test
        void shouldHandleNullOrBlank() {
            FeignRetryUtils.RetryAfterDecoder decoder = new FeignRetryUtils.RetryAfterDecoder();
            assertThat(decoder.apply(null)).isZero();
            assertThat(decoder.apply("")).isZero();
            assertThat(decoder.apply("  ")).isZero();
        }

        @Test
        void shouldHandleInvalidFormat() {
            FeignRetryUtils.RetryAfterDecoder decoder = new FeignRetryUtils.RetryAfterDecoder();
            assertThat(decoder.apply("invalid")).isZero();
        }

        static class TestDecoder extends FeignRetryUtils.RetryAfterDecoder {
            @Override
            protected long currentTimeMillis() {
                return 1000L;
            }
        }

        @Test
        void shouldHandleDecimalSeconds() {
            FeignRetryUtils.RetryAfterDecoder decoder = new TestDecoder();
            assertThat(decoder.apply("30.0")).isEqualTo(31000L);
            assertThat(decoder.apply("30")).isEqualTo(31000L);
        }

        @Test
        void shouldHandleMalformedDecimal() {
            FeignRetryUtils.RetryAfterDecoder decoder = new FeignRetryUtils.RetryAfterDecoder();
            assertThat(decoder.apply("30.0.0")).isZero();
        }

        @Test
        void shouldHandleCustomDateTimeFormatter() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            FeignRetryUtils.RetryAfterDecoder decoder = new FeignRetryUtils.RetryAfterDecoder(formatter);
            String dateStr = "2021-12-31 23:59:59 GMT";
            long expectedEpoch = ZonedDateTime.parse(dateStr, formatter).toInstant().toEpochMilli();
            assertThat(decoder.apply(dateStr)).isEqualTo(expectedEpoch);
        }
    }
}
