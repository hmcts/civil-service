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
        log.info("Determining logged in solicitor for caseId: {}", caseData.getCcdCaseReference());
        var updatedCaseData = caseData.toBuilder();

        updateSolicitorRoles(callbackParams, updatedCaseData);
        updateCompanyOrOrganisationStatus(caseData, updatedCaseData);

        log.info("Updated case data for logged in solicitor for caseId: {}", caseData.getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.build().toMap(objectMapper))
                .build();
    }

    private void updateSolicitorRoles(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        var caseData = callbackParams.getCaseData();
        log.info("Updating solicitor roles for caseId: {}", caseData.getCcdCaseReference());

        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            log.debug("Solicitor has RESPONDENTSOLICITORONE role for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.isRespondent1(YES);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            log.debug("Solicitor has RESPONDENTSOLICITORTWO role for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(YES);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
            log.debug("Solicitor has APPLICANTSOLICITORONE role for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(YES);
        }
    }

    private void updateCompanyOrOrganisationStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating company or organisation status for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2())) {
            log.debug("Updating company or organisation status for Respondent2 for caseId: {}", caseData.getCcdCaseReference());
            updateCompanyOrOrganisationStatusForRespondent2(caseData, updatedCaseData);
        } else {
            log.debug("Updating company or organisation status for Respondent1 for caseId: {}", caseData.getCcdCaseReference());
            updateCompanyOrOrganisationStatusForRespondent1(caseData, updatedCaseData);
        }
    }

    private void updateCompanyOrOrganisationStatusForRespondent2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating company or organisation status for Respondent2 for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent2DetailsForClaimDetailsTab() != null
                && ("Company".equals(caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
                || "Organisation".equals(caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue()))) {
            log.debug("Respondent2 is a Company or Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.neitherCompanyNorOrganisation(NO);
        } else {
            log.debug("Respondent2 is neither a Company nor Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.neitherCompanyNorOrganisation(YES);
        }
    }

    private void updateCompanyOrOrganisationStatusForRespondent1(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating company or organisation status for Respondent1 for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1DetailsForClaimDetailsTab() != null
                && ("Company".equals(caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
                || "Organisation".equals(caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue()))) {
            log.debug("Respondent1 is a Company or Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.neitherCompanyNorOrganisation(NO);
        } else {
            log.debug("Respondent1 is neither a Company nor Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.neitherCompanyNorOrganisation(YES);
        }
    }

    private boolean solicitorHasCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Checking if solicitor has case role '{}' for caseId: {}", caseRole, caseData.getCcdCaseReference());

        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        boolean hasRole = coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                userInfo.getUid(),
                caseRole
        );

        log.debug("Solicitor has case role '{}' for caseId {}: {}", caseRole, caseData.getCcdCaseReference(), hasRole);
        return hasRole;
    }
}
