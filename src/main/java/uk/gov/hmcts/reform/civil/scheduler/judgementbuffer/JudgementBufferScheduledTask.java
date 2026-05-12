package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Component
@Slf4j
@AllArgsConstructor
public class JudgementBufferScheduledTask implements ScheduledTask {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final JudgementEligibilityChecker judgementEligibilityChecker;

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("JudgementBufferScheduledTask::accept case {}", caseId);

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            String.valueOf(caseId),
            CaseEvent.ISSUE_DEFAULT_JUDGEMENT_SPEC
        );

        CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());

        if (judgementEligibilityChecker.isEligibleForJudgement(caseData)) {
            CaseDataContent caseDataContent = coreCaseDataService.caseDataContentFromStartEventResponse(
                startEventResponse,
                caseData.toMap(objectMapper)
            );
            caseDataContent.getEvent().setSummary("Issue Judgement");
            caseDataContent.getEvent().setDescription("Issue Judgement after Judgement Buffer");
            coreCaseDataService.submitUpdate(String.valueOf(caseId), caseDataContent);
        } else {
            log.info("JudgementBufferScheduledTask::accept case {} is not eligible for Issue Default Judgement Spec", caseId);
        }
    }
}
