package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Service
@RequiredArgsConstructor
public class DjOrderDetailsService {

    private final DjLocationAndToggleService locationAndToggleService;
    private final DjDisposalDirectionsService disposalDirectionsService;
    private final DjTrialDirectionsService trialDirectionsService;
    private final UserService userService;

    public CaseData populateTrialDisposalScreen(DirectionsOrderTaskContext context) {
        CallbackParams callbackParams = context.callbackParams();
        CaseData caseData = locationAndToggleService.prepareLocationsAndToggles(context);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        String judgeNameTitle = userDetails.getFullName();

        disposalDirectionsService.populateDisposalDirections(caseDataBuilder, judgeNameTitle);
        trialDirectionsService.populateTrialDirections(caseDataBuilder, judgeNameTitle);

        return caseDataBuilder.build();
    }

    public CaseData applyHearingSelections(CaseData caseData, CallbackVersion version) {
        return locationAndToggleService.applyHearingSelections(caseData, version);
    }

}
