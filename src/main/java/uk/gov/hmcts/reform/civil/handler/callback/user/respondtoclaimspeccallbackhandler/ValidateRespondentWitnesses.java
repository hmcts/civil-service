package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
        log.info("Executing validation for callbackParams");
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (!ONE_V_ONE.equals(scenario)) {
            log.info("Handling multi-party scenario");
            return handleMultiPartyScenario(callbackParams, caseData);
        }
        log.info("Validating R1 witnesses");
        return validateR1Witnesses(caseData);
    }

    private CallbackResponse handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData) {
        if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(
            callbackParams,
            RESPONDENTSOLICITORONE
        )) {
            log.info("Solicitor represents only one of respondents: RESPONDENTSOLICITORONE");
            return validateR1Witnesses(caseData);
        } else if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(
            callbackParams,
            RESPONDENTSOLICITORTWO
        )) {
            log.info("Solicitor represents only one of respondents: RESPONDENTSOLICITORTWO");
            return validateWitnesses(caseData.getRespondent2DQ());
        } else if (shouldValidateRespondent2Witnesses(caseData)) {
            log.info("Validating respondent 2 witnesses");
            return validateWitnesses(caseData.getRespondent2DQ());
        }
        log.info("Defaulting to validate R1 witnesses");
        return validateR1Witnesses(caseData);
    }

    private boolean shouldValidateRespondent2Witnesses(CaseData caseData) {
        log.info("Checking if should validate respondent 2 witnesses");
        return respondent2HasSameLegalRep(caseData)
            && isRespondentResponseDifferent(caseData)
            && hasRespondent2DQ(caseData)
            && hasRespondent2DQWitnesses(caseData);
    }

    private boolean isRespondentResponseDifferent(CaseData caseData) {
        log.info("Checking if respondent response is different");
        return caseData.getRespondentResponseIsSame() != null
            && caseData.getRespondentResponseIsSame() == NO;
    }

    private boolean hasRespondent2DQ(CaseData caseData) {
        log.info("Checking if respondent 2 DQ exists");
        return caseData.getRespondent2DQ() != null;
    }

    private boolean hasRespondent2DQWitnesses(CaseData caseData) {
        log.info("Checking if respondent 2 DQ witnesses exist");
        return caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null;
    }

    private CallbackResponse validateR1Witnesses(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getRespondent1DQWitnessesRequiredSpec() == YES
            && caseData.getRespondent1DQWitnessesDetailsSpec() == null) {
            log.error("Witness details required");
            errors.add("Witness details required");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        log.info("Checking if respondent 2 has same legal representative");
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
