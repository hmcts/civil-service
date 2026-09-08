package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.client.HearingsApi;
import uk.gov.hmcts.reform.hmc.exception.HmcException;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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

        @Bean
        HearingsApi hearingNoticeApi() {
            return mock(HearingsApi.class);
        }

        @Bean
        AuthTokenGenerator authTokenGenerator() {
            return mock(AuthTokenGenerator.class);
        }
    }

    @Autowired
    private HearingsApi hearingNoticeApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HearingsService hearingNoticeService;

    private static final String USER_TOKEN = "user_token";
    private static final String SERVICE_TOKEN = "service_token";
    private static final String HEARING_ID = "hearing_id";
    private static final String HMC_STATUS = "Listed";
    private static final Long CASE_ID = new BigInteger("1234123412341234").longValue();

    private final RetryableException timeoutException = mock(RetryableException.class);

    @BeforeEach
    void setUp() {
        reset(hearingNoticeApi, authTokenGenerator);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(timeoutException.getMessage()).thenReturn("timeout message");
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
            .thenThrow(buildFeignException(404, "not found response body"));

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
            .thenThrow(buildFeignException(502, "server error response body"));

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

    @Test
    void shouldRetryHearingResponseServerErrorsBeforeRecovering() {
        when(hearingNoticeApi.getHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID),
            nullable(Boolean.class)))
            .thenThrow(buildFeignException(502, "server error response body"));

        assertThrows(HmcException.class, () -> hearingNoticeService.getHearingResponse(USER_TOKEN, HEARING_ID));

        verify(hearingNoticeApi, times(3)).getHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID),
            nullable(Boolean.class));
    }

    @Test
    void shouldRetryHearingResponseTimeoutsBeforeRecovering() {
        when(hearingNoticeApi.getHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID),
            nullable(Boolean.class)))
            .thenThrow(timeoutException);

        assertThrows(HmcException.class, () -> hearingNoticeService.getHearingResponse(USER_TOKEN, HEARING_ID));

        verify(hearingNoticeApi, times(3)).getHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID),
            nullable(Boolean.class));
    }

    @Test
    void shouldRetryPartiesNotifiedResponsesServerErrorsBeforeRecovering() {
        when(hearingNoticeApi.getPartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID)))
            .thenThrow(buildFeignException(502, "server error response body"));

        assertThrows(HmcException.class, () -> hearingNoticeService.getPartiesNotifiedResponses(USER_TOKEN, HEARING_ID));

        verify(hearingNoticeApi, times(3)).getPartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID));
    }

    @Test
    void shouldRetryPartiesNotifiedResponsesTimeoutsBeforeRecovering() {
        when(hearingNoticeApi.getPartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID)))
            .thenThrow(timeoutException);

        assertThrows(HmcException.class, () -> hearingNoticeService.getPartiesNotifiedResponses(USER_TOKEN, HEARING_ID));

        verify(hearingNoticeApi, times(3)).getPartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(HEARING_ID));
    }

    @Test
    void shouldRetryUpdatePartiesNotifiedResponsesServerErrorsBeforeRecovering() {
        LocalDateTime receivedAt = LocalDateTime.of(2023, 5, 1, 15, 0);
        uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified partiesNotified =
            new uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified();

        when(hearingNoticeApi.updatePartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            any(),
            eq(HEARING_ID),
            anyInt(),
            any()))
            .thenThrow(buildFeignException(502, "server error response body"));

        assertThrows(HmcException.class, () -> hearingNoticeService.updatePartiesNotifiedResponse(
            USER_TOKEN,
            HEARING_ID,
            1,
            receivedAt,
            partiesNotified));

        verify(hearingNoticeApi, times(3)).updatePartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            any(),
            eq(HEARING_ID),
            anyInt(),
            any());
    }

    @Test
    void shouldRetryUpdatePartiesNotifiedResponsesTimeoutsBeforeRecovering() {
        LocalDateTime receivedAt = LocalDateTime.of(2023, 5, 1, 15, 0);
        uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified partiesNotified =
            new uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified();

        when(hearingNoticeApi.updatePartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            any(),
            eq(HEARING_ID),
            anyInt(),
            any()))
            .thenThrow(timeoutException);

        assertThrows(HmcException.class, () -> hearingNoticeService.updatePartiesNotifiedResponse(
            USER_TOKEN,
            HEARING_ID,
            1,
            receivedAt,
            partiesNotified));

        verify(hearingNoticeApi, times(3)).updatePartiesNotifiedRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            any(),
            eq(HEARING_ID),
            anyInt(),
            any());
    }

    @Test
    void shouldRetryUnNotifiedHearingResponsesServerErrorsBeforeRecovering() {
        LocalDateTime from = LocalDateTime.of(2023, 5, 1, 15, 0);
        LocalDateTime to = LocalDateTime.of(2023, 5, 6, 15, 0);

        when(hearingNoticeApi.getUnNotifiedHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq("hmcts-service-code"),
            any(),
            any()))
            .thenThrow(buildFeignException(502, "server error response body"));

        assertThrows(HmcException.class, () -> hearingNoticeService.getUnNotifiedHearingResponses(
            USER_TOKEN,
            "hmcts-service-code",
            from,
            to));

        verify(hearingNoticeApi, times(3)).getUnNotifiedHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq("hmcts-service-code"),
            any(),
            any());
    }

    @Test
    void shouldRetryUnNotifiedHearingResponsesTimeoutsBeforeRecovering() {
        LocalDateTime from = LocalDateTime.of(2023, 5, 1, 15, 0);
        LocalDateTime to = LocalDateTime.of(2023, 5, 6, 15, 0);

        when(hearingNoticeApi.getUnNotifiedHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq("hmcts-service-code"),
            any(),
            any()))
            .thenThrow(timeoutException);

        assertThrows(HmcException.class, () -> hearingNoticeService.getUnNotifiedHearingResponses(
            USER_TOKEN,
            "hmcts-service-code",
            from,
            to));

        verify(hearingNoticeApi, times(3)).getUnNotifiedHearingRequest(
            eq(USER_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq("hmcts-service-code"),
            any(),
            any());
    }

    private FeignException buildFeignException(int status, String body) {
        return FeignException.errorStatus(
            "HearingsApi#test",
            Response.builder()
                .status(status)
                .reason("reason")
                .request(Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null))
                .headers(Map.of())
                .body(body, UTF_8)
                .build()
        );
    }
}
