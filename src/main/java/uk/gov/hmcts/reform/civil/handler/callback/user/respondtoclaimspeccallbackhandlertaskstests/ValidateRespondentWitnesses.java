package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ValidateRespondentWitnesses implements CaseTask, WitnessesValidator {

    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing ValidateRespondentWitnesses task with callbackParams: {}", callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (!ONE_V_ONE.equals(scenario)) {
            log.info("Handling multi-party scenario for caseData: {}", caseData);
            return handleMultiPartyScenario(callbackParams, caseData);
        }
        log.info("Validating R1 witnesses for caseData: {}", caseData);
        return validateR1Witnesses(caseData);
    }

    private CallbackResponse handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData) {
        if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            log.info("Solicitor represents only one of the respondents (RESPONDENTSOLICITORONE)");
            return validateR1Witnesses(caseData);
        } else if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            log.info("Solicitor represents only one of the respondents (RESPONDENTSOLICITORTWO)");
            return validateWitnesses(caseData.getRespondent2DQ());
        } else if (isRespondent2HasDifferentLegalRep(caseData)) {
            log.info("Respondent 2 has a different legal representative");
            return validateWitnesses(caseData.getRespondent2DQ());
        }
        log.info("Defaulting to validate R1 witnesses");
        return validateR1Witnesses(caseData);
    }

    private boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        boolean result = respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, caseRole);
        log.info("isSolicitorRepresentsOnlyOneOfRespondents for caseRole {}: {}", caseRole, result);
        return result;
    }

    private boolean isRespondent2HasDifferentLegalRep(CaseData caseData) {
        boolean result = isSameLegalRep(caseData) && isRespondentResponseNotSame(caseData) && isRespondent2DQWitnesses(caseData);
        log.info("isRespondent2HasDifferentLegalRep: {}", result);
        return result;
    }

    private boolean isSameLegalRep(CaseData caseData) {
        boolean result = respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData);
        log.info("isSameLegalRep: {}", result);
        return result;
    }

    private boolean isRespondentResponseNotSame(CaseData caseData) {
        boolean result = caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO;
        log.info("isRespondentResponseNotSame: {}", result);
        return result;
    }

    private boolean isRespondent2DQWitnesses(CaseData caseData) {
        boolean result = caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null;
        log.info("isRespondent2DQWitnesses: {}", result);
        return result;
    }

    private CallbackResponse validateR1Witnesses(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getRespondent1DQWitnessesRequiredSpec() == YES
            && caseData.getRespondent1DQWitnessesDetailsSpec() == null) {
            log.info("Witness details required for Respondent 1");
            errors.add("Witness details required");
        }
        log.info("Validation errors for R1 witnesses: {}", errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
