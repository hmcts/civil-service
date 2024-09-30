package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing ValidateRespondentExperts task");
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        log.debug("MultiPartyScenario: {}", scenario);

        if (!ONE_V_ONE.equals(scenario)) {
            log.info("Handling multi-party scenario");
            return handleMultiPartyScenario(callbackParams, caseData);
        }
        log.info("Validating experts for single-party scenario");
        return validateExperts(caseData.getRespondent1DQ());
    }

    private CallbackResponse handleMultiPartyScenario(CallbackParams callbackParams, CaseData caseData) {
        log.debug("Handling multi-party scenario with case data: {}", caseData);
        if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            log.info("Solicitor represents only respondent one");
            return validateExperts(caseData.getRespondent1DQ());
        } else if (respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            log.info("Solicitor represents only respondent two");
            return validateExperts(caseData.getRespondent2DQ());
        } else if (isValidateRespondent2Experts(caseData)) {
            log.info("Validating experts for respondent two");
            return validateExperts(caseData.getRespondent2DQ());
        }
        log.info("Defaulting to validate experts for respondent one");
        return validateExperts(caseData.getRespondent1DQ());
    }

    private boolean isValidateRespondent2Experts(CaseData caseData) {
        boolean result = isSameLegalRep(caseData)
            && isRespondentResponseNotSame(caseData)
            && hasRespondent2DQExperts(caseData);
        log.debug("isValidateRespondent2Experts: {}", result);
        return result;
    }

    private boolean isSameLegalRep(CaseData caseData) {
        boolean result = respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData);
        log.debug("isSameLegalRep: {}", result);
        return result;
    }

    private boolean isRespondentResponseNotSame(CaseData caseData) {
        boolean result = caseData.getRespondentResponseIsSame() != null
            && caseData.getRespondentResponseIsSame() == NO;
        log.debug("isRespondentResponseNotSame: {}", result);
        return result;
    }

    private boolean hasRespondent2DQExperts(CaseData caseData) {
        boolean result = caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQExperts() != null;
        log.debug("hasRespondent2DQExperts: {}", result);
        return result;
    }
}
