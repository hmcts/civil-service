package uk.gov.hmcts.reform.hearings.hearingnotice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hearings.hearingnotice.client.HearingNoticeApi;
import uk.gov.hmcts.reform.hearings.hearingnotice.exception.GetHearingException;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.HearingGetResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingNoticeService {

    private final HearingNoticeApi hearingNoticeApi;
    private final AuthTokenGenerator authTokenGenerator;

    public HearingGetResponse getHearingResponse(String authToken, String hearingId) throws GetHearingException {
        log.debug("Sending Get Hearings with Hearing ID {}", hearingId);
        try {
            HearingGetResponse response = hearingNoticeApi.getHearingRequest(
                authToken,
                authTokenGenerator.generate(),
                hearingId,
                null);
            if (response == null) {
                log.error("Failed to retrieve hearing with Id: {} from HMC", hearingId);
                throw new GetHearingException(hearingId);
            }
            return response;
        } catch (Exception ex)  {
            log.error("Failed to retrieve hearing with Id: {} from HMC", hearingId);
            throw new GetHearingException(hearingId, ex);
        }
    }
}
