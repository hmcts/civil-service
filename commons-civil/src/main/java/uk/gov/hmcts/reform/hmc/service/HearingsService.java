package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.client.HearingsApi;
import uk.gov.hmcts.reform.hmc.exception.HmcException;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;

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
}
