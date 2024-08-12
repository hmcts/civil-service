package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondToClaimCallbackHandlerTasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.user.RespondentService;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class ValidateRespondentExperts implements CaseTask, ExpertsValidator {

    private final RespondentService respondentService;

    public ValidateRespondentExperts(RespondentService respondentService, RespondentService respondentService1) {
        this.respondentService = respondentService;
    }

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            return validateExperts(caseData.getRespondent1DQ());
        }

        if (respondentService.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateExperts(caseData.getRespondent1DQ());
        }

        if (respondentService.solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateExperts(caseData.getRespondent2DQ());

        }
        if (shouldValidateRespondent2Experts(caseData)) {
            return validateExperts(caseData.getRespondent2DQ());
        }

        return validateExperts(caseData.getRespondent1DQ());
    }

    private boolean shouldValidateRespondent2Experts(CaseData caseData) {
        return (respondentService.respondent2HasSameLegalRep(caseData)
            && (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO)
            && (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQExperts() != null));
    }
}
