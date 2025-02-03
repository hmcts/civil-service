package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Component
@Slf4j
public class ValidateUnavailableDates implements CaseTask {

    private final UnavailableDateValidator unavailableDateValidator;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;

    public ValidateUnavailableDates(UnavailableDateValidator unavailableDateValidator,
                                    IStateFlowEngine stateFlowEngine,
                                    CoreCaseUserService coreCaseUserService,
                                    UserService userService) {
        this.unavailableDateValidator = unavailableDateValidator;
        this.stateFlowEngine = stateFlowEngine;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        log.info("Validating Unavailable Dates for Case ID {} :", caseData.getCcdCaseReference());
        Hearing hearing = caseData.getRespondent1DQ().getHearing();

        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
            && (isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            || isRespondent2ResponseAndHearingDifferent(caseData))) {
            hearing = caseData.getRespondent2DQ().getHearing();
        }

        List<String> errors = unavailableDateValidator.validate(hearing);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private boolean isRespondent2ResponseAndHearingDifferent(CaseData caseData) {
        return isRespondent2SameLegalRep(caseData)
            && caseData.getRespondentResponseIsSame() != null
            && caseData.getRespondentResponseIsSame() == NO
            && caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getHearing() != null;
    }

    private boolean isRespondent2SameLegalRep(CaseData caseData) {
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

