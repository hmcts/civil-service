package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondToClaimCallbackHandlerTasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.user.RespondentService;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class ValidateRespondentWitnesses implements CaseTask, WitnessesValidator {

    private final RespondentService respondentService;

    public ValidateRespondentWitnesses(RespondentService respondentService) {
        this.respondentService = respondentService;
    }

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            return validateWitnesses(caseData.getRespondent1DQ());
        }

        if (respondentService.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateWitnesses(caseData.getRespondent1DQ());

        }
        if (respondentService.solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateWitnesses(caseData.getRespondent2DQ());
        }

        if (shouldValidateRespondent2Witnesses(caseData)) {
            return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
        }

        return validateWitnesses(caseData.getRespondent1DQ());
    }

    private boolean shouldValidateRespondent2Witnesses(CaseData caseData) {
        return respondentService.respondent2HasSameLegalRep(caseData)
            && caseData.getRespondentResponseIsSame() == NO
            && caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null;
    }

}
