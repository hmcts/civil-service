package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
        log.info("Executing validation for case ID: {}", caseData.getCcdCaseReference());

        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            log.info("Handling multi-party scenario for case ID: {}", caseData.getCcdCaseReference());
            return handleMultiPartyScenario(callbackParams, caseData);
        }

        log.info("Validating experts for respondent 1 in case ID: {}", caseData.getCcdCaseReference());
        return validateExperts(caseData.getRespondent1DQ());
    }

    private CallbackResponse handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData) {
        if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            log.info("Solicitor represents only respondent 1 in case ID: {}", caseData.getCcdCaseReference());
            return validateExperts(caseData.getRespondent1DQ());
        } else if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            log.info("Solicitor represents only respondent 2 in case ID: {}", caseData.getCcdCaseReference());
            return validateExperts(caseData.getRespondent2DQ());
        } else if (isRespondent2HasDifferentLegalRepAndExperts(caseData)) {
            log.info("Respondent 2 has different legal representation and experts in case ID: {}", caseData.getCcdCaseReference());
            return validateExperts(caseData.getRespondent2DQ());
        }

        log.info("Validating experts for respondent 1 in case ID: {}", caseData.getCcdCaseReference());
        return validateExperts(caseData.getRespondent1DQ());
    }

    private boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        boolean result = respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, caseRole);
        log.debug("Solicitor represents only one of respondents for case role {}: {}", caseRole, result);
        return result;
    }

    private boolean isRespondent2HasDifferentLegalRepAndExperts(CaseData caseData) {
        boolean result = hasSameLegalRep(caseData) && isRespondentResponseDifferent(caseData) && hasRespondent2Experts(caseData);
        log.debug("Respondent 2 has different legal rep and experts: {}", result);
        return result;
    }

    private boolean hasSameLegalRep(CaseData caseData) {
        boolean result = respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData);
        log.debug("Respondent 2 has same legal rep: {}", result);
        return result;
    }

    private boolean isRespondentResponseDifferent(CaseData caseData) {
        boolean result = caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO;
        log.debug("Respondent response is different: {}", result);
        return result;
    }

    private boolean hasRespondent2Experts(CaseData caseData) {
        boolean result = caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQExperts() != null;
        log.debug("Respondent 2 has experts: {}", result);
        return result;
    }
}
