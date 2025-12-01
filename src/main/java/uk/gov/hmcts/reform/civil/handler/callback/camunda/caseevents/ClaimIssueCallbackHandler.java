package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCESS_CLAIM_ISSUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimIssueCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(PROCESS_CLAIM_ISSUE);
    private static final String TASK_ID = "IssueClaim";

    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::addClaimNotificationDeadlineAndNextDeadline);
    }

    private CallbackResponse addClaimNotificationDeadlineAndNextDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime deadline = deadlinesCalculator.addMonthsToDateAtMidnight(4, caseData.getIssueDate());
        caseData.setClaimNotificationDeadline(deadline);
        caseData.setNextDeadline(deadline.toLocalDate());

        clearSubmitterId(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void clearSubmitterId(CaseData caseData) {
        IdamUserDetails userDetails = caseData.getApplicantSolicitor1UserDetails();
        IdamUserDetails solicitorDetails = new IdamUserDetails();
        solicitorDetails.setEmail(userDetails.getEmail());
        caseData.setApplicantSolicitor1UserDetails(solicitorDetails);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
