package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.client.HearingsApi;
import uk.gov.hmcts.reform.hmc.exception.HmcException;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int BACKOFF_DELAY_MS = 500;

    private final HearingsApi hearingNoticeApi;
    private final AuthTokenGenerator authTokenGenerator;
    @Value("${role-assignment-service.api.url:#{null}}")
    private String roleAssignmentUrl;
    @Value("${core_case_data.api.url:#{null}}")
    private String dataStoreUrl;

    @Retryable(
        retryFor = {FeignException.class, RetryableException.class},
        noRetryFor = FeignException.FeignClientException.class,
        maxAttempts = MAX_ATTEMPTS,
        backoff = @Backoff(delay = BACKOFF_DELAY_MS)
    )
    public HearingGetResponse getHearingResponse(String authToken, String hearingId) throws HmcException {
        log.debug("Sending Get Hearings with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getHearingRequest(
                authToken,
                authTokenGenerator.generate(),
                dataStoreUrl,
                roleAssignmentUrl,
                hearingId,
                null);
        } catch (RetryableException ex) {
            log.error("Failed to retrieve hearing with Id: {} from HMC. Retryable HMC failure: {}", hearingId, ex.getMessage(), ex);
            throw ex;
        } catch (FeignException ex)  {
            log.error(
                "Failed to retrieve hearing with Id: {} from HMC. Status: {}, response body: {}",
                hearingId,
                ex.status(),
                ex.contentUTF8(),
                ex
            );
            if (isClientError(ex)) {
                throw new HmcException(ex);
            }
            throw ex;
        }
    }

    @Retryable(
        retryFor = {FeignException.class, RetryableException.class},
        noRetryFor = FeignException.FeignClientException.class,
        maxAttempts = MAX_ATTEMPTS,
        backoff = @Backoff(delay = BACKOFF_DELAY_MS)
    )
    public PartiesNotifiedResponses getPartiesNotifiedResponses(String authToken, String hearingId) {
        log.debug("Requesting Get Parties Notified with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getPartiesNotifiedRequest(
                authToken,
                authTokenGenerator.generate(),
                dataStoreUrl,
                roleAssignmentUrl,
                hearingId);
        } catch (RetryableException e) {
            log.error(
                "Failed to retrieve parties notified with Id: {} from HMC. Retryable HMC failure: {}",
                hearingId,
                e.getMessage(),
                e
            );
            throw e;
        } catch (FeignException e) {
            log.error(
                "Failed to retrieve parties notified with Id: {} from HMC. Status: {}, response body: {}",
                hearingId,
                e.status(),
                e.contentUTF8(),
                e
            );
            if (isClientError(e)) {
                throw new HmcException(e);
            }
            throw e;
        }
    }

    @Retryable(
        retryFor = {FeignException.class, RetryableException.class},
        noRetryFor = FeignException.FeignClientException.class,
        maxAttempts = MAX_ATTEMPTS,
        backoff = @Backoff(delay = BACKOFF_DELAY_MS)
    )
    public ResponseEntity updatePartiesNotifiedResponse(String authToken, String hearingId,
                                                        int requestVersion, LocalDateTime receivedDateTime,
                                                        PartiesNotified payload) {
        try {
            return hearingNoticeApi.updatePartiesNotifiedRequest(
                authToken,
                authTokenGenerator.generate(),
                dataStoreUrl,
                roleAssignmentUrl,
                payload,
                hearingId,
                requestVersion,
                receivedDateTime
            );
        } catch (RetryableException ex) {
            log.error(
                "Failed to update partiesNotified with Id: {} from HMC. Retryable HMC failure: {}",
                hearingId,
                ex.getMessage(),
                ex
            );
            throw ex;
        } catch (FeignException ex)  {
            log.error(
                "Failed to update partiesNotified with Id: {} from HMC. Status: {}, response body: {}",
                hearingId,
                ex.status(),
                ex.contentUTF8(),
                ex
            );
            if (isClientError(ex)) {
                throw new HmcException(ex);
            }
            throw ex;
        }
    }

    @Retryable(
        retryFor = {FeignException.class, RetryableException.class},
        noRetryFor = FeignException.FeignClientException.class,
        maxAttempts = MAX_ATTEMPTS,
        backoff = @Backoff(delay = BACKOFF_DELAY_MS)
    )
    public UnNotifiedHearingResponse getUnNotifiedHearingResponses(String authToken, String hmctsServiceCode,
                                                                   LocalDateTime hearingStartDateFrom,
                                                                   LocalDateTime hearingStartDateTo) {
        log.debug("Requesting UnNotified Hearings");
        try {
            return hearingNoticeApi.getUnNotifiedHearingRequest(
                authToken,
                authTokenGenerator.generate(),
                dataStoreUrl,
                roleAssignmentUrl,
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo);
        } catch (RetryableException e) {
            log.error("Failed to retrieve unnotified hearings. Retryable HMC failure: {}", e.getMessage(), e);
            throw e;
        } catch (FeignException e) {
            log.error("Failed to retrieve unnotified hearings. Status: {}, response body: {}", e.status(), e.contentUTF8(), e);
            if (isClientError(e)) {
                throw new HmcException(e);
            }
            throw e;
        }
    }

    @Retryable(
        retryFor = {FeignException.class, RetryableException.class},
        noRetryFor = FeignException.FeignClientException.class,
        maxAttempts = MAX_ATTEMPTS,
        backoff = @Backoff(delay = BACKOFF_DELAY_MS)
    )
    public HearingsResponse getHearings(String authToken, Long caseId, String status) {
        log.debug("Requesting Hearings for case: {}", caseId);
        try {
            return hearingNoticeApi.getHearings(
                authToken,
                authTokenGenerator.generate(),
                dataStoreUrl,
                roleAssignmentUrl,
                caseId,
                status);
        } catch (RetryableException e) {
            log.error(
                "Failed to retrieve hearings for case: {} with status: {} from HMC. Retryable HMC failure: {}",
                caseId,
                status,
                e.getMessage(),
                e
            );
            throw e;
        } catch (FeignException e) {
            log.error(
                "Failed to retrieve hearings for case: {} with status: {} from HMC. Status: {}, response body: {}",
                caseId,
                status,
                e.status(),
                e.contentUTF8(),
                e
            );
            if (isClientError(e)) {
                throw new HmcException(e);
            }
            throw e;
        }
    }

    @Recover
    HearingGetResponse recoverGetHearingResponse(Exception ex, String authToken, String hearingId) {
        if (ex instanceof FeignException feignException) {
            log.error(
                "Failed to retrieve hearing with Id: {} from HMC after retries. Status: {}, response body: {}",
                hearingId,
                feignException.status(),
                feignException.contentUTF8(),
                feignException
            );
        } else {
            log.error(
                "Failed to retrieve hearing with Id: {} from HMC after retries. Retryable HMC failure: {}",
                hearingId,
                ex.getMessage(),
                ex
            );
        }
        throw new HmcException(ex);
    }

    @Recover
    PartiesNotifiedResponses recoverGetPartiesNotifiedResponses(Exception ex, String authToken, String hearingId) {
        if (ex instanceof FeignException feignException) {
            log.error(
                "Failed to retrieve parties notified with Id: {} from HMC after retries. Status: {}, response body: {}",
                hearingId,
                feignException.status(),
                feignException.contentUTF8(),
                feignException
            );
        } else {
            log.error(
                "Failed to retrieve parties notified with Id: {} from HMC after retries. Retryable HMC failure: {}",
                hearingId,
                ex.getMessage(),
                ex
            );
        }
        throw new HmcException(ex);
    }

    @Recover
    ResponseEntity recoverUpdatePartiesNotifiedResponse(Exception ex, String authToken, String hearingId,
                                                        int requestVersion, LocalDateTime receivedDateTime,
                                                        PartiesNotified payload) {
        if (ex instanceof FeignException feignException) {
            log.error(
                "Failed to update partiesNotified with Id: {} from HMC after retries. Status: {}, response body: {}",
                hearingId,
                feignException.status(),
                feignException.contentUTF8(),
                feignException
            );
        } else {
            log.error(
                "Failed to update partiesNotified with Id: {} from HMC after retries. Retryable HMC failure: {}",
                hearingId,
                ex.getMessage(),
                ex
            );
        }
        throw new HmcException(ex);
    }

    @Recover
    UnNotifiedHearingResponse recoverGetUnNotifiedHearingResponses(Exception ex, String authToken,
                                                                   String hmctsServiceCode,
                                                                   LocalDateTime hearingStartDateFrom,
                                                                   LocalDateTime hearingStartDateTo) {
        if (ex instanceof FeignException feignException) {
            log.error(
                "Failed to retrieve unnotified hearings after retries. Status: {}, response body: {}",
                feignException.status(),
                feignException.contentUTF8(),
                feignException
            );
        } else {
            log.error(
                "Failed to retrieve unnotified hearings after retries. Retryable HMC failure: {}",
                ex.getMessage(),
                ex
            );
        }
        throw new HmcException(ex);
    }

    @Recover
    HearingsResponse recoverGetHearings(Exception ex, String authToken, Long caseId, String status) {
        if (ex instanceof FeignException feignException) {
            log.error(
                "Failed to retrieve hearings for case: {} with status: {} from HMC after retries. Status: {}, response body: {}",
                caseId,
                status,
                feignException.status(),
                feignException.contentUTF8(),
                feignException
            );
        } else {
            log.error(
                "Failed to retrieve hearings for case: {} with status: {} from HMC after retries. Retryable HMC failure: {}",
                caseId,
                status,
                ex.getMessage(),
                ex
            );
        }
        throw new HmcException(ex);
    }

    private boolean isClientError(FeignException ex) {
        return ex instanceof FeignException.FeignClientException;
    }

}
