package uk.gov.hmcts.reform.hearings.hearingnotice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hearings.hearingnotice.client.HearingNoticeApi;
import uk.gov.hmcts.reform.hearings.hearingnotice.exception.HearingNoticeException;
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
}
