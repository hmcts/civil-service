package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
public class ValidateRespondentWitnesses implements CaseTask, WitnessesValidator {

    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (!ONE_V_ONE.equals(scenario)) {
            return handleMultiPartyScenario(callbackParams, caseData);
        }
        return validateR1Witnesses(caseData);
    }

    private CallbackResponse handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData) {
        if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateR1Witnesses(caseData);
        } else if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateWitnesses(caseData.getRespondent2DQ());
        } else if (isRespondent2HasDifferentLegalRep(caseData)) {
            return validateWitnesses(caseData.getRespondent2DQ());
        }
        return validateR1Witnesses(caseData);
    }

    private boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        return respondToClaimSpecUtils.solicitorRepresentsOnlyOneOfRespondents(callbackParams, caseRole);
    }

    private boolean isRespondent2HasDifferentLegalRep(CaseData caseData) {
        return hasSameLegalRep(caseData) && isRespondentResponseNotSame(caseData) && hasRespondent2DQWitnesses(caseData);
    }

    private boolean hasSameLegalRep(CaseData caseData) {
        return respondToClaimSpecUtils.respondent2HasSameLegalRep(caseData);
    }

    private boolean isRespondentResponseNotSame(CaseData caseData) {
        return caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO;
    }

    private boolean hasRespondent2DQWitnesses(CaseData caseData) {
        return caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null;
    }

    private CallbackResponse validateR1Witnesses(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getRespondent1DQWitnessesRequiredSpec() == YES
            && caseData.getRespondent1DQWitnessesDetailsSpec() == null) {
            errors.add("Witness details required");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}

