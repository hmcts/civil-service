package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateRespondentExperts implements CaseTask, ExpertsValidator {

    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (!ONE_V_ONE.equals(scenario)) {
            return handleMultiPartyScenario(callbackParams, caseData);
        }
        return validateExperts(caseData.getRespondent1DQ());
    }

    private CallbackResponse handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData) {
        if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateExperts(caseData.getRespondent1DQ());
        } else if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateExperts(caseData.getRespondent2DQ());
        } else if (isRespondent2WithDifferentLegalRep(caseData)) {
            return validateExperts(caseData.getRespondent2DQ());
        }
        return validateExperts(caseData.getRespondent1DQ());
    }

    private boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        return respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, caseRole);
    }

    private boolean isRespondent2WithDifferentLegalRep(CaseData caseData) {
        return hasSameLegalRep(caseData) && isResponseDifferent(caseData) && hasRespondent2DQExperts(caseData);
    }

    private boolean hasSameLegalRep(CaseData caseData) {
        return respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData);
    }

    private boolean isResponseDifferent(CaseData caseData) {
        return caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO;
    }

    private boolean hasRespondent2DQExperts(CaseData caseData) {
        return caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQExperts() != null;
    }
}
