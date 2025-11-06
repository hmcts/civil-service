package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.dj.DjDisposalDirectionsService;
import uk.gov.hmcts.reform.civil.service.dj.DjLocationAndToggleService;
import uk.gov.hmcts.reform.civil.service.dj.DjTrialDirectionsService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;

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
        if (!V_1.equals(version)) {
            return caseData;
        }

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        if (caseData.getHearingMethodValuesDisposalHearingDJ() != null
            && caseData.getHearingMethodValuesDisposalHearingDJ().getValue() != null) {
            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearingDJ().getValue().getLabel();
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.disposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.disposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.disposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesTrialHearingDJ() != null
            && caseData.getHearingMethodValuesTrialHearingDJ().getValue() != null) {
            String trialHearingMethodLabel = caseData.getHearingMethodValuesTrialHearingDJ().getValue().getLabel();
            if (trialHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.trialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
            } else if (trialHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.trialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
            } else if (trialHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.trialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
            }
        }

        return updatedData.build();
    }

}
