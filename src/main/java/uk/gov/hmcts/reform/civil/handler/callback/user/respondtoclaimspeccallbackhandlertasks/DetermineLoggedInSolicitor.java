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

        updateSolicitorRoles(callbackParams, caseData);
        updateCompanyOrOrganisationStatus(caseData, caseData);

        log.info("Updated case data for logged in solicitor for caseId: {}", caseData.getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private void updateSolicitorRoles(CallbackParams callbackParams, CaseData caseData) {
        log.info("Updating solicitor roles for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());

        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            log.info("Solicitor has RESPONDENTSOLICITORONE role for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());
            caseData.setIsRespondent1(YES);
            caseData.setIsRespondent2(NO);
            caseData.setIsApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            log.info("Solicitor has RESPONDENTSOLICITORTWO role for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());
            caseData.setIsRespondent1(NO);
            caseData.setIsRespondent2(YES);
            caseData.setIsApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
            log.info("Solicitor has APPLICANTSOLICITORONE role for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());
            caseData.setIsRespondent1(NO);
            caseData.setIsRespondent2(NO);
            caseData.setIsApplicant1(YES);
        }
    }

    private void updateCompanyOrOrganisationStatus(CaseData caseData, CaseData updatedCaseData) {
        log.info("Updating company or organisation status for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2())) {
            log.info("Updating company or organisation status for Respondent2 for caseId: {}", caseData.getCcdCaseReference());
            updateCompanyOrOrganisationStatusForRespondent2(caseData, updatedCaseData);
        } else {
            log.info("Updating company or organisation status for Respondent1 for caseId: {}", caseData.getCcdCaseReference());
            updateCompanyOrOrganisationStatusForRespondent1(caseData, updatedCaseData);
        }
    }

    private void updateCompanyOrOrganisationStatusForRespondent2(CaseData caseData, CaseData updatedCaseData) {
        log.info("Updating company or organisation status for Respondent2 for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent2DetailsForClaimDetailsTab() != null
                && ("Company".equals(caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
                || "Organisation".equals(caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue()))) {
            log.info("Respondent2 is a Company or Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.setNeitherCompanyNorOrganisation(NO);
        } else {
            log.info("Respondent2 is neither a Company nor Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.setNeitherCompanyNorOrganisation(YES);
        }
    }

    private void updateCompanyOrOrganisationStatusForRespondent1(CaseData caseData, CaseData updatedCaseData) {
        log.info("Updating company or organisation status for Respondent1 for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1DetailsForClaimDetailsTab() != null
                && ("Company".equals(caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
                || "Organisation".equals(caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue()))) {
            log.info("Respondent1 is a Company or Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.setNeitherCompanyNorOrganisation(NO);
        } else {
            log.info("Respondent1 is neither a Company nor Organisation for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.setNeitherCompanyNorOrganisation(YES);
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

        log.info("Solicitor has case role '{}' for caseId {}: {}", caseRole, caseData.getCcdCaseReference(), hasRole);
        return hasRole;
    }
}
