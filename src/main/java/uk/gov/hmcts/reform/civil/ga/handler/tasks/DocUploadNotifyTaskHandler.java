package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.search.GaEvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;

@Slf4j
@RequiredArgsConstructor
@Component
public class DocUploadNotifyTaskHandler extends BaseExternalTaskHandler {

    private final GaEvidenceUploadNotificationSearchService caseSearchService;
    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        List<GeneralApplicationCaseData> cases = caseSearchService.getApplications().stream()
                .map(caseDetailsConverter::toGeneralApplicationCaseData).toList();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(this::fireEventForStateChange);
        return ExternalTaskData.builder().build();
    }

    private void fireEventForStateChange(GeneralApplicationCaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Firing event EVIDENCE_UPLOAD_CHECK to notify applications with newly "
                + "uploaded documents "
                + "for caseId: {}", caseId);

        coreCaseDataService.triggerGaEvent(caseId, GA_EVIDENCE_UPLOAD_CHECK,
                Map.of());
        log.info("Checking state for caseId: {}", caseId);
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
