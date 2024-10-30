package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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

    public CallbackResponse execute(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var caseId = caseData.getCcdCaseReference();
        log.info("Executing DetermineLoggedInSolicitor for case ID: {}", caseId);

        var updatedCaseData = caseData.toBuilder();

        updateSolicitorRoles(callbackParams, updatedCaseData, caseId);
        updateCompanyOrOrganisationStatus(caseData, updatedCaseData, caseId);

        log.info("Completed DetermineLoggedInSolicitor for case ID: {}", caseId);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private void updateSolicitorRoles(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData, Long caseId) {
        log.debug("Updating solicitor roles for case ID: {}", caseId);
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            updatedCaseData.isRespondent1(YES);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(NO);
            log.info("Solicitor role updated to RESPONDENTSOLICITORONE for case ID: {}", caseId);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(YES);
            updatedCaseData.isApplicant1(NO);
            log.info("Solicitor role updated to RESPONDENTSOLICITORTWO for case ID: {}", caseId);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(YES);
            log.info("Solicitor role updated to APPLICANTSOLICITORONE for case ID: {}", caseId);
        }
    }

    private void updateCompanyOrOrganisationStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, Long caseId) {
        log.debug("Updating company or organisation status for case ID: {}", caseId);
        if (YES.equals(caseData.getIsRespondent2())) {
            if (isCompanyOrOrganisation(caseData.getRespondent2DetailsForClaimDetailsTab())) {
                updatedCaseData.neitherCompanyNorOrganisation(NO);
                log.info("Respondent 2 is a company or organisation for case ID: {}", caseId);
            } else {
                updatedCaseData.neitherCompanyNorOrganisation(YES);
                log.info("Respondent 2 is neither a company nor organisation for case ID: {}", caseId);
            }
        } else {
            if (isCompanyOrOrganisation(caseData.getRespondent1DetailsForClaimDetailsTab())) {
                updatedCaseData.neitherCompanyNorOrganisation(NO);
                log.info("Respondent 1 is a company or organisation for case ID: {}", caseId);
            } else {
                updatedCaseData.neitherCompanyNorOrganisation(YES);
                log.info("Respondent 1 is neither a company nor organisation for case ID: {}", caseId);
            }
        }
    }

    private boolean isCompanyOrOrganisation(Party party) {
        return party != null && ("Company".equals(party.getPartyTypeDisplayValue())
            || "Organisation".equals(party.getPartyTypeDisplayValue()));
    }

    private boolean solicitorHasCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        boolean hasRole = coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
        log.debug("Solicitor has case role {}: {}", caseRole, hasRole);
        return hasRole;
    }
}
