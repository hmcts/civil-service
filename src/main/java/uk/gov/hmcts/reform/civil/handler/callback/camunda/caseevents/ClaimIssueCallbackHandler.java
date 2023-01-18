package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
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
        CaseData.CaseDataBuilder caseDataUpdated = caseData.toBuilder();
        caseDataUpdated
            .claimNotificationDeadline(deadline)
            .nextDeadline(deadline.toLocalDate());

        clearSubmitterId(caseData, caseDataUpdated);

        // don't display cases in unassigned case list before claim notified workaround.
        clearOrganisationPolicyId(caseData, caseDataUpdated);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    private void clearSubmitterId(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        IdamUserDetails userDetails = caseData.getApplicantSolicitor1UserDetails();
        caseDataBuilder
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(userDetails.getEmail()).build())
            .build();
    }

    private void clearOrganisationPolicyId(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (YES.equals(caseData.getRespondent1OrgRegistered())) {
            caseDataBuilder.respondent1OrganisationIDCopy(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID());

            caseDataBuilder.respondent1OrganisationPolicy(
                caseData
                    .getRespondent1OrganisationPolicy()
                    .toBuilder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build())
                    .build()
            );
        }

        if (NO.equals(caseData.getRespondent2SameLegalRepresentative())) {
            caseDataBuilder.respondent2OrganisationIDCopy(
                caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID());

            caseDataBuilder.respondent2OrganisationPolicy(
                caseData
                    .getRespondent2OrganisationPolicy()
                    .toBuilder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build())
                    .build()
            );
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
