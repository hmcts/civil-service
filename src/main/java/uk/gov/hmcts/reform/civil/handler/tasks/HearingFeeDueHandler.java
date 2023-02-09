package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class HearingFeeDueHandler implements BaseExternalTaskHandler {

    private final HearingFeeDueSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                CaseDetails detailsWithData = coreCaseDataService.getCase(caseDetails.getId());
                CaseData caseData = caseDetailsConverter.toCaseData(detailsWithData);
                PaymentDetails hearingFeePaymentDetails = caseData.getHearingFeePaymentDetails();

                if (hearingFeePaymentDetails != null
                        && hearingFeePaymentDetails.getStatus() == PaymentStatus.SUCCESS) {
                    log.info("Current case status '{}'", caseDetails.getState());
                    applicationEventPublisher.publishEvent(new HearingFeePaidEvent(caseDetails.getId()));
                } else if (hearingFeePaymentDetails == null
                            || hearingFeePaymentDetails.getStatus() == PaymentStatus.FAILED) {
                    log.info("Current case status '{}'", caseDetails.getState());
                    applicationEventPublisher.publishEvent(new HearingFeeUnpaidEvent(caseDetails.getId()));
                }
            } catch (Exception e) {
                //Continue for other cases if there is some error in some cases, as we don't want
                // to stop processing other valid cases because error happened in some.
                //We log the error to leave a trace that something needs to be looked into for failed cases
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
    }
}
