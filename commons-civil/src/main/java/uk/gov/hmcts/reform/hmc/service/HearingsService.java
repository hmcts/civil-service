package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.client.HearingsApi;
import uk.gov.hmcts.reform.hmc.exception.HmcException;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {

    private final HearingsApi hearingNoticeApi;
    private final AuthTokenGenerator authTokenGenerator;

    public HearingGetResponse getHearingResponse(String authToken, String hearingId) throws HmcException {
        log.debug("Sending Get Hearings with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getHearingRequest(
                authToken,
                authTokenGenerator.generate(),
                hearingId,
                null);
        } catch (FeignException ex)  {
            log.error("Failed to retrieve hearing with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }

    public PartiesNotifiedResponses getPartiesNotifiedResponses(String authToken, String hearingId) {
        log.debug("Requesting Get Parties Notified with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getPartiesNotifiedRequest(
                authToken,
                authTokenGenerator.generate(),
                hearingId);
        } catch (FeignException e) {
            log.error("Failed to retrieve patries notified with Id: %s from HMC", hearingId);
            throw new HmcException(e);
        }
    }

    public ResponseEntity updatePartiesNotifiedResponse(String authToken, String hearingId,
                                                        Long requestVersion, LocalDateTime receivedDateTime,
                                                        PartiesNotified payload) {
        try {
            return hearingNoticeApi.updatePartiesNotifiedRequest(
                authToken,
                authTokenGenerator.generate(),
                payload,
                hearingId,
                requestVersion.intValue(),
                receivedDateTime
            );
        } catch (FeignException ex)  {
            log.error("Failed to update partiesNotified with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }

    public UnNotifiedHearingResponse getUnNotifiedHearingResponses(String authToken, String hmctsServiceCode,
                                                                   LocalDateTime hearingStartDateFrom,
                                                                   LocalDateTime hearingStartDateTo) {
        log.debug("Requesting UnNotified Hearings");
        try {
            return hearingNoticeApi.getUnNotifiedHearingRequest(
                authToken,
                authTokenGenerator.generate(),
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo);
        } catch (FeignException e) {
            log.error("Failed to retrieve unnotified hearings");
            throw new HmcException(e);
        }
    }
}
