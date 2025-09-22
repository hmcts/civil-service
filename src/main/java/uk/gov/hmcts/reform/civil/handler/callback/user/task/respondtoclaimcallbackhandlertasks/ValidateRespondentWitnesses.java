package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Component
@Slf4j
public class ValidateRespondentWitnesses implements CaseTask, WitnessesValidator {

    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;

    public ValidateRespondentWitnesses(IStateFlowEngine stateFlowEngine, CoreCaseUserService coreCaseUserService, UserService userService) {
        this.stateFlowEngine = stateFlowEngine;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Validating respondent witnesses for Case ID {} :", caseData.getCcdCaseReference());

        if (ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            return validateWitnesses(caseData.getRespondent1DQ());
        }

        if (isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
            return validateWitnesses(caseData.getRespondent1DQ());

        }
        if (isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            return validateWitnesses(caseData.getRespondent2DQ());
        }

        if (isRespondent2ResponseAndWitnessesDifferent(caseData)) {
            return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
        }

        return validateWitnesses(caseData.getRespondent1DQ());
    }

    private boolean isRespondent2ResponseAndWitnessesDifferent(CaseData caseData) {
        return respondent2HasSameLegalRep(caseData)
            && caseData.getRespondentResponseIsSame() == NO
            && caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private boolean isSolicitorRepresentingOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }
}


