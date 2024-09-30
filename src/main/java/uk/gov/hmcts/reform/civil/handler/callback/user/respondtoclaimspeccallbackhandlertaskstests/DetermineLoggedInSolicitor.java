package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
@Slf4j
public class DetermineLoggedInSolicitor implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final ObjectMapper objectMapper;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing DetermineLoggedInSolicitor task for case ID: {}", callbackParams.getCaseData().getCcdCaseReference());

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        updateSolicitorRoles(callbackParams, updatedCaseData);
        updateCompanyOrOrganisationFlag(caseData, updatedCaseData);

        log.info("DetermineLoggedInSolicitor task completed for case ID: {}", caseData.getCcdCaseReference());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private void updateSolicitorRoles(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating solicitor roles for case ID: {}", callbackParams.getCaseData().getCcdCaseReference());

        if (isSolicitorWithCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            log.info("User is RESPONDENTSOLICITORONE for case ID: {}", callbackParams.getCaseData().getCcdCaseReference());
            updatedCaseData.isRespondent1(YES).isRespondent2(NO).isApplicant1(NO);
        } else if (isSolicitorWithCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            log.info("User is RESPONDENTSOLICITORTWO for case ID: {}", callbackParams.getCaseData().getCcdCaseReference());
            updatedCaseData.isRespondent1(NO).isRespondent2(YES).isApplicant1(NO);
        } else if (isSolicitorWithCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
            log.info("User is APPLICANTSOLICITORONE for case ID: {}", callbackParams.getCaseData().getCcdCaseReference());
            updatedCaseData.isRespondent1(NO).isRespondent2(NO).isApplicant1(YES);
        }
    }

    private void updateCompanyOrOrganisationFlag(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating company or organisation flag for case ID: {}", caseData.getCcdCaseReference());

        boolean isRespondent2 = YES.equals(caseData.getIsRespondent2());
        Party respondentDetails = isRespondent2 ? caseData.getRespondent2DetailsForClaimDetailsTab() : caseData.getRespondent1DetailsForClaimDetailsTab();
        boolean isCompanyOrOrganisation = respondentDetails != null
            && ("Company".equals(respondentDetails.getPartyTypeDisplayValue()) || "Organisation".equals(respondentDetails.getPartyTypeDisplayValue()));

        updatedCaseData.neitherCompanyNorOrganisation(isCompanyOrOrganisation ? NO : YES);

        log.info("Company or organisation flag updated to {} for case ID: {}", isCompanyOrOrganisation ? NO : YES, caseData.getCcdCaseReference());
    }

    private boolean isSolicitorWithCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        boolean hasRole = coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );

        log.debug("User {} has role {}: {}", userInfo.getUid(), caseRole, hasRole);
        return hasRole;
    }
}
