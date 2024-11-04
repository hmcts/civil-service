package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class DetermineLoggedInSolicitor implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final ObjectMapper objectMapper;

    public CallbackResponse execute(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var updatedCaseData = caseData.toBuilder();

        updateSolicitorRoles(callbackParams, updatedCaseData);
        updateCompanyOrOrganisationStatus(caseData, updatedCaseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private void updateSolicitorRoles(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            updatedCaseData.isRespondent1(YES);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(YES);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(YES);
        }
    }

    private void updateCompanyOrOrganisationStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent2())) {
            updateCompanyOrOrganisationStatusForRespondent2(caseData, updatedCaseData);
        } else {
            updateCompanyOrOrganisationStatusForRespondent1(caseData, updatedCaseData);
        }
    }

    private void updateCompanyOrOrganisationStatusForRespondent2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (caseData.getRespondent2DetailsForClaimDetailsTab() != null
            && ("Company".equals(caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
            || "Organisation".equals(caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue()))) {
            updatedCaseData.neitherCompanyNorOrganisation(NO);
        } else {
            updatedCaseData.neitherCompanyNorOrganisation(YES);
        }
    }

    private void updateCompanyOrOrganisationStatusForRespondent1(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (caseData.getRespondent1DetailsForClaimDetailsTab() != null
            && ("Company".equals(caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
            || "Organisation".equals(caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue()))) {
            updatedCaseData.neitherCompanyNorOrganisation(NO);
        } else {
            updatedCaseData.neitherCompanyNorOrganisation(YES);
        }
    }

    private boolean solicitorHasCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }
}
