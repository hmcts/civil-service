package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT_SOLICITOR1_SPEC;

@Service
@RequiredArgsConstructor
public class AssignCaseToUserForSpecHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ASSIGN_CASE_TO_APPLICANT_SOLICITOR1_SPEC);
    public static final String TASK_ID = "CaseAssignmentToApplicantSolicitor1ForSpec";

    private final CoreCaseUserService coreCaseUserService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
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
        if (toggleService.isLrSpecEnabled()) {
            return EVENTS;
        } else {
            return Collections.emptyList();
        }
    }

    private CallbackResponse assignSolicitorCaseRole(CallbackParams callbackParams) {
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        String caseId = caseData.getCcdCaseReference().toString();
        IdamUserDetails userDetails = caseData.getApplicantSolicitor1UserDetails();
        String submitterId = userDetails.getId();
        String organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();

        coreCaseUserService.assignCase(caseId, submitterId, organisationId, CaseRole.APPLICANTSOLICITORONESPEC);
        coreCaseUserService.removeCreatorRoleCaseAssignment(caseId, submitterId, organisationId);
        // This sets the "supplementary_data" value "HmctsServiceId to the Spec service ID AAA6
        setSupplementaryData(caseData.getCcdCaseReference(), callbackParams);

        return SubmittedCallbackResponse.builder().build();

    }

    private void setSupplementaryData(Long caseId, CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        Map<String, Map<String, Map<String, Object>>> supplementaryDataCivil = new HashMap<>();
        supplementaryDataCivil.put("supplementary_data_updates",
                                   singletonMap("$set", singletonMap("HMCTSServiceId",
                                                                     paymentsConfiguration.getSpecSiteId())));
        coreCaseDataService.setSupplementaryData(authorisation, caseId, supplementaryDataCivil);

    }
}
