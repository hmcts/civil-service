package uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpec;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
public class RespondToClaimSpecUtilsCourtLocation {

    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    public Optional<LocationRefData> getCourtLocationDefendant1(CaseData caseData, CallbackParams callbackParams) {
        return getCourtLocation(
            caseData.getRespondent1DQ() != null ? caseData.getRespondent1DQ().getRespondToCourtLocation().getResponseCourtLocations() : null,
            callbackParams
        );
    }

    public Optional<LocationRefData> getCourtLocationDefendant2(CaseData caseData, CallbackParams callbackParams) {
        return getCourtLocation(
            caseData.getRespondent2DQ() != null ? caseData.getRespondent2DQ().getRespondToCourtLocation2().getResponseCourtLocations() : null,
            callbackParams
        );
    }

    private Optional<LocationRefData> getCourtLocation(DynamicList respondToCourtLocation, CallbackParams callbackParams) {
        if (respondToCourtLocation != null && respondToCourtLocation.getListItems() != null) {
            List<LocationRefData> locationData = fetchLocationData(callbackParams);
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(locationData, respondToCourtLocation);
            return Optional.ofNullable(courtLocation);
        } else {
            return Optional.empty();
        }
    }

    public List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
