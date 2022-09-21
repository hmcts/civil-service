package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.event.StrikeOutEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.search.CaseHearingFeePaidSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class HearingFeePaidHandler implements BaseExternalTaskHandler {

    private final CaseHearingFeePaidSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                Long caseId = caseDetails.getId();
                CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

                if (caseData.getHearingFeePaymentDetails().getStatus() == PaymentStatus.SUCCESS) {
                    log.info("Current case status '{}'", caseDetails.getState());
                    applicationEventPublisher.publishEvent(new StrikeOutEvent(caseDetails.getId()));
                } else if (caseData.getHearingFeePaymentDetails().getStatus() == PaymentStatus.FAILED) {
                    log.info("Current case status '{}'", caseDetails.getState());
                    applicationEventPublisher.publishEvent(new StrikeOutEvent(caseDetails.getId()));
                }
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
    }
}
