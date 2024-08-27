package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Component
public class ValidateRespondentExperts implements CaseTask, ExpertsValidator {

    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;

    public ValidateRespondentExperts(IStateFlowEngine stateFlowEngine, CoreCaseUserService coreCaseUserService, UserService userService) {
        this.stateFlowEngine = stateFlowEngine;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
    }

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            return validateExperts(caseData.getRespondent1DQ());
        }

        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateExperts(caseData.getRespondent1DQ());
        }

        if (solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateExperts(caseData.getRespondent2DQ());

        }
        if (hasRespondent2DifferentResponseAndExperts(caseData)) {
            return validateExperts(caseData.getRespondent2DQ());
        }

        return validateExperts(caseData.getRespondent1DQ());
    }

    private boolean hasRespondent2DifferentResponseAndExperts(CaseData caseData) {
        return (respondent2HasSameLegalRep(caseData)
            && (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO)
            && (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQExperts() != null));
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        return solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, caseRole);
    }

    private boolean solicitorRepresentsOnlyOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
