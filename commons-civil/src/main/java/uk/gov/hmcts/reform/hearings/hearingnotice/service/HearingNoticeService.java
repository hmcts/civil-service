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

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingNoticeService {

    private final HearingNoticeApi hearingNoticeApi;
    private final AuthTokenGenerator authTokenGenerator;

    public PartiesNotifiedResponses getPartiesNotifiedResponses(String authToken, String hearingId) {
        log.debug("Requesting Get Parties Notified with Hearing ID {}", hearingId);
        try {
            PartiesNotifiedResponses hearingResponse = hearingNoticeApi.getPartiesNotifiedRequest(
                authToken,
                authTokenGenerator.generate(),
                hearingId);
            return hearingResponse;
        } catch (FeignException e) {
            log.error("Failed to retrieve hearing with Id: %s from HMC", hearingId);
            throw new HearingNoticeException(e);
        }
    }

    public UnNotifiedPartiesResponse getUnNotifiedHearingResponses(String authToken, String hmctsServiceCode,
                                                                   LocalDateTime hearingStartDateFrom,
                                                                   LocalDateTime hearingStartDateTo) {
        log.debug("Requesting UnNotified Hearings");
        try {
            UnNotifiedPartiesResponse hearingResponse = hearingNoticeApi.getUnNotifiedHearingRequest(
                authToken,
                authTokenGenerator.generate(),
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo);
            return hearingResponse;
        } catch (Exception e) {
            log.error("Failed to retrieve unnotified hearings");
            throw new HearingNoticeException(e);
        }
    }
}
