package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT1;

@Service
@RequiredArgsConstructor
public class AssignCaseToLipUserHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        ASSIGN_CASE_TO_APPLICANT1);

    public static final String TASK_ID = "CaseAssignmentToApplicant1";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Callback handler received illegal event: %s";

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseUserService coreCaseUserService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::assignUserCaseRole
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (ASSIGN_CASE_TO_APPLICANT1.equals(caseEvent)) {
            return TASK_ID;
        } else {
            throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, caseEvent));
        }
    }

    private CallbackResponse assignUserCaseRole(CallbackParams callbackParams) {
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());

        IdamUserDetails claimantUserDetails = caseData.getClaimantUserDetails();
        String caseId = caseData.getCcdCaseReference().toString();
        String submitId = claimantUserDetails.getId();

        coreCaseUserService.assignCase(caseId, submitId, null, CaseRole.CLAIMANT);
        coreCaseUserService.removeCreatorRoleCaseAssignment(caseId, submitId, null);

        return SubmittedCallbackResponse.builder().build();
    }
}
