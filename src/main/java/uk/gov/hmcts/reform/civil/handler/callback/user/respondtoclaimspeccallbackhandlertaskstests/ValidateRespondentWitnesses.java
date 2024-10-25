package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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
@Slf4j
public class ValidateRespondentWitnesses implements CaseTask, WitnessesValidator {

    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                return validateR1Witnesses(caseData);
            } else if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
                return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
            } else if (respondent2HasSameLegalRep(caseData)
                && caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO
                && caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null) {
                return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
            }
        }
        return validateR1Witnesses(caseData);
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

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}