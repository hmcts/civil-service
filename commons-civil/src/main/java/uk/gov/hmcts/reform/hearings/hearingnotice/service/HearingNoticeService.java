package uk.gov.hmcts.reform.hearings.hearingnotice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hearings.hearingnotice.client.HearingNoticeApi;
import uk.gov.hmcts.reform.hearings.hearingnotice.exception.HearingNoticeException;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.UnNotifiedPartiesResponse;
import java.time.LocalDateTime;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.HearingGetResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingNoticeService {

    private final HearingNoticeApi hearingNoticeApi;
    private final AuthTokenGenerator authTokenGenerator;

    public HearingGetResponse getHearingResponse(String authToken, String hearingId) throws HearingNoticeException {
        log.debug("Sending Get Hearings with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getHearingRequest(
                authToken,
                authTokenGenerator.generate(),
                hearingId,
                null);
        } catch (FeignException ex)  {
            log.error("Failed to retrieve hearing with Id: {} from HMC", hearingId);
            throw new HearingNoticeException(ex);
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
            throw new HearingNoticeException(e);
        }
    }

    public UnNotifiedPartiesResponse getUnNotifiedHearingResponses(String authToken, String hmctsServiceCode,
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
            throw new HearingNoticeException(e);
        }
    }
}
