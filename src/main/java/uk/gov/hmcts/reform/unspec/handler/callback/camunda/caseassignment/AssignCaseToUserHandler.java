package uk.gov.hmcts.reform.unspec.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.CaseRole;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.IdamUserDetails;
import uk.gov.hmcts.reform.unspec.service.CoreCaseUserService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT_SOLICITOR1;

@Service
@RequiredArgsConstructor
public class AssignCaseToUserHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ASSIGN_CASE_TO_APPLICANT_SOLICITOR1);
    public static final String TASK_ID = "CaseAssignmentToApplicantSolicitor1";

    private final CoreCaseUserService coreCaseUserService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::assignSolicitorCaseRole
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

        coreCaseUserService.assignCase(caseId, submitterId, organisationId, CaseRole.APPLICANTSOLICITORONE);
        coreCaseUserService.removeCreatorRoleCaseAssignment(caseId, submitterId, organisationId);

        CaseData updated = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(userDetails.getEmail()).build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updated.toMap(objectMapper))
            .build();
    }
}
