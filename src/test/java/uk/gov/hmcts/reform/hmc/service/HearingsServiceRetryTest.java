package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.client.HearingsApi;
import uk.gov.hmcts.reform.hmc.exception.HmcException;

import java.math.BigInteger;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    HearingsService.class,
    HearingsServiceRetryTest.TestRetryConfig.class
})
class HearingsServiceRetryTest {

    @EnableRetry(proxyTargetClass = true)
    @Configuration
    static class TestRetryConfig {
    }

    @MockBean
    private HearingsApi hearingNoticeApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HearingsService hearingNoticeService;

    private static final String USER_TOKEN = "user_token";
    private static final String SERVICE_TOKEN = "service_token";
    private static final String HEARING_ID = "hearing_id";
    private static final String HMC_STATUS = "Listed";
    private static final Long CASE_ID = new BigInteger("1234123412341234").longValue();

    private final FeignException notFoundFeignException = new FeignException.NotFound(
        "not found message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "not found response body".getBytes(UTF_8),
        Map.of());

    private final FeignException internalServerError = new FeignException.InternalServerError(
        "server error message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "server error response body".getBytes(UTF_8),
        Map.of());

    private final RetryableException timeoutException = new RetryableException(
        504,
        "timeout message",
        GET,
        null,
        (Long) null,
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null));

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void shouldNotRetryClientErrors() {
        when(hearingNoticeApi.getHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID),
            nullable(Boolean.class)))
            .thenThrow(notFoundFeignException);

        assertThrows(HmcException.class, () -> hearingNoticeService.getHearingResponse(USER_TOKEN, HEARING_ID));

        verify(hearingNoticeApi, times(1)).getHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID),
            nullable(Boolean.class));
    }

    @Test
    void shouldRetryServerErrorsBeforeRecovering() {
        when(hearingNoticeApi.getHearings(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            anyLong(),
            eq(HMC_STATUS)))
            .thenThrow(internalServerError);

        assertThrows(HmcException.class, () -> hearingNoticeService.getHearings(USER_TOKEN, CASE_ID, HMC_STATUS));

        verify(hearingNoticeApi, times(3)).getHearings(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(CASE_ID),
            eq(HMC_STATUS));
    }

    @Test
    void shouldRetryTimeoutsBeforeRecovering() {
        when(hearingNoticeApi.getHearings(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            anyLong(),
            eq(HMC_STATUS)))
            .thenThrow(timeoutException);

        assertThrows(HmcException.class, () -> hearingNoticeService.getHearings(USER_TOKEN, CASE_ID, HMC_STATUS));

        verify(hearingNoticeApi, times(3)).getHearings(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(CASE_ID),
            eq(HMC_STATUS));
    }
}
