package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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
public class DetermineLoggedInSolicitor implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final ObjectMapper objectMapper;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        updateSolicitorRoles(callbackParams, updatedCaseData);
        updateCompanyOrOrganisationFlag(caseData, updatedCaseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private void updateSolicitorRoles(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            updatedCaseData.isRespondent1(YES).isRespondent2(NO).isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            updatedCaseData.isRespondent1(NO).isRespondent2(YES).isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
            updatedCaseData.isRespondent1(NO).isRespondent2(NO).isApplicant1(YES);
        }
    }

    private void updateCompanyOrOrganisationFlag(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        boolean isRespondent2 = YES.equals(caseData.getIsRespondent2());
        Party respondentDetails = isRespondent2 ? caseData.getRespondent2DetailsForClaimDetailsTab() : caseData.getRespondent1DetailsForClaimDetailsTab();
        boolean isCompanyOrOrganisation = respondentDetails != null &&
            ("Company".equals(respondentDetails.getPartyTypeDisplayValue()) || "Organisation".equals(respondentDetails.getPartyTypeDisplayValue()));

        updatedCaseData.neitherCompanyNorOrganisation(isCompanyOrOrganisation ? NO : YES);
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
