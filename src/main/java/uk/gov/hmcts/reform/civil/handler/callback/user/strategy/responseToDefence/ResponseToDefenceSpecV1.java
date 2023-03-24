package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.responseToDefence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
public class ResponseToDefenceSpecV1 extends ResponseToDefenceSpecStrategy {

    private final FeatureToggleService featureToggleService;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    @Override
    public CallbackResponse populateCaseData(CallbackParams callbackParams, ObjectMapper objectMapper) {
        var caseDataBuilder = updateCaseDataWithRespondent1Copy(callbackParams).builder();
        if(featureToggleService.isCourtLocationDynamicListEnabled()){
            List<LocationRefData> locationRefData = locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(BEARER_TOKEN).toString());
            caseDataBuilder.applicant1DQ(
                Applicant1DQ.builder().applicant1DQRequestedCourt(
                    RequestedCourt.builder().responseCourtLocations(
                        courtLocationUtils.getLocationsFromList(locationRefData)).build()
                ).build());
        }

        return  AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
