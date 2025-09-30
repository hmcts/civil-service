package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

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
public class ValidateRespondentExpertsSpec implements CaseTask, ExpertsValidator {

    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing respondent experts validation for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());

        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (!ONE_V_ONE.equals(scenario)) {
            log.debug("CaseId {}: Handling multi-party scenario", caseData.getCcdCaseReference());
            return handleMultiPartyScenario(callbackParams, caseData);
        }

        return validateExperts(caseData.getRespondent1DQ());
    }

    private CallbackResponse handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData) {
        log.info("Handling multi-party scenario for caseId: {}", caseData.getCcdCaseReference());

        if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            log.debug("CaseId {}: Solicitor represents only Respondent 1", caseData.getCcdCaseReference());
            return validateExperts(caseData.getRespondent1DQ());
        } else if (isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            log.debug("CaseId {}: Solicitor represents only Respondent 2", caseData.getCcdCaseReference());
            return validateExperts(caseData.getRespondent2DQ());
        } else if (isRespondent2WithDifferentLegalRep(caseData)) {
            log.debug("CaseId {}: Respondent 2 has different legal representation", caseData.getCcdCaseReference());
            return validateExperts(caseData.getRespondent2DQ());
        }

        return validateExperts(caseData.getRespondent1DQ());
    }

    private boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        log.debug("Checking if solicitor represents only one of the respondents for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());
        return respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, caseRole);
    }

    private boolean isRespondent2WithDifferentLegalRep(CaseData caseData) {
        log.debug("Checking if Respondent 2 has different legal representation for caseId: {}", caseData.getCcdCaseReference());
        return hasSameLegalRep(caseData) && isResponseDifferent(caseData) && hasRespondent2DQExperts(caseData);
    }

    private boolean hasSameLegalRep(CaseData caseData) {
        log.debug("Checking if Respondent 2 has the same legal representative for caseId: {}", caseData.getCcdCaseReference());
        return respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData);
    }

    private boolean isResponseDifferent(CaseData caseData) {
        log.debug("Checking if Respondent 2's response is different for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO;
    }

    private boolean hasRespondent2DQExperts(CaseData caseData) {
        log.debug("Checking if Respondent 2 has DQ experts for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQExperts() != null;
    }
}
