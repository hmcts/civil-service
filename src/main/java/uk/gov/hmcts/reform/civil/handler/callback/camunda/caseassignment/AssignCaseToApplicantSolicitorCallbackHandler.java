package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT_SOLICITOR1;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseToApplicantSolicitorCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        ASSIGN_CASE_TO_APPLICANT_SOLICITOR1);

    public static final String TASK_ID = "CaseAssignmentToApplicantSolicitor1";

    private final CoreCaseUserService coreCaseUserService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final PaymentsConfiguration paymentsConfiguration;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(SUBMITTED), this::assignSolicitorCaseRole
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse assignSolicitorCaseRole(CallbackParams callbackParams) {
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        String caseId = caseData.getCcdCaseReference().toString();
        IdamUserDetails userDetails = caseData.getApplicantSolicitor1UserDetails();
        String submitterId = userDetails.getId();
        String organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();

        log.info("Assigning case {} to solicitor {}", caseId, submitterId);
        coreCaseUserService.assignCase(caseId, submitterId, organisationId, CaseRole.APPLICANTSOLICITORONE);
        coreCaseUserService.removeCreatorRoleCaseAssignment(caseId, submitterId, organisationId);
        String siteId = UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? paymentsConfiguration.getSiteId() : paymentsConfiguration.getSpecSiteId();
        setSupplementaryData(caseData.getCcdCaseReference(), siteId);

        return SubmittedCallbackResponse.builder().build();
    }

    private CallbackResponse saveSupplementaryData(CallbackParams callbackParams) {
        // This sets the "supplementary_data" value "HmctsServiceId to the Unspec service ID AAA7 or Spec service ID AAA6
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        String siteId = UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? paymentsConfiguration.getSiteId() : paymentsConfiguration.getSpecSiteId();
        setSupplementaryData(caseData.getCcdCaseReference(), siteId);
        return SubmittedCallbackResponse.builder().build();
    }

    private void setSupplementaryData(Long caseId, String siteId) {
        Map<String, Map<String, Map<String, Object>>> supplementaryDataCivil = new HashMap<>();
        supplementaryDataCivil.put(
            "supplementary_data_updates",
            singletonMap("$set", singletonMap("HMCTSServiceId", siteId))
        );
        coreCaseDataService.setSupplementaryData(caseId, supplementaryDataCivil);
    }
}
