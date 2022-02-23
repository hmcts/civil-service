package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT_SOLICITOR1;

@Service
public class AssignCaseToUserHandler extends AbstractAssignCaseToUserHandler {

    private static final List<CaseEvent> EVENTS = List.of(ASSIGN_CASE_TO_APPLICANT_SOLICITOR1);
    public static final String TASK_ID = "CaseAssignmentToApplicantSolicitor1";

    public AssignCaseToUserHandler(CoreCaseUserService coreCaseUserService, CaseDetailsConverter caseDetailsConverter, ObjectMapper objectMapper) {
        super(coreCaseUserService, caseDetailsConverter, objectMapper);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
