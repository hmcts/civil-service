package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
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
public class RespondToClaimSpecCaseDataHandlerSolicitorDetails {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final ObjectMapper objectMapper;

    CallbackResponse determineLoggedInSolicitor(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        if (setSolicitorRole(callbackParams, updatedCaseData)) {
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
            updatedCaseData.isRespondent1(NO)
                .isRespondent2(NO)
                .isApplicant1(YES);
        }

        updateCompanyOrOrganisationFlag(caseData, updatedCaseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private boolean setSolicitorRole(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            updatedCaseData.isRespondent1(YES)
                .isRespondent2(NO);
            return true;
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            updatedCaseData.isRespondent1(NO)
                .isRespondent2(YES);
            return true;
        }
        return false;
    }

    private void updateCompanyOrOrganisationFlag(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent2())) {
            updatedCaseData.neitherCompanyNorOrganisation(
                isCompanyOrOrganisation(caseData.getRespondent2DetailsForClaimDetailsTab()) ? NO : YES);
        } else {
            updatedCaseData.neitherCompanyNorOrganisation(
                isCompanyOrOrganisation(caseData.getRespondent1DetailsForClaimDetailsTab()) ? NO : YES);
        }
    }

    private boolean isCompanyOrOrganisation(Party partyDetails) {
        return partyDetails != null && ("Company".equals(partyDetails.getPartyTypeDisplayValue())
            || "Organisation".equals(partyDetails.getPartyTypeDisplayValue()));
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
