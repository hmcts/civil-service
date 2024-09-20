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
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class HearingFeeDueHandler implements BaseExternalTaskHandler {

    private final HearingFeeDueSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                CaseDetails detailsWithData = coreCaseDataService.getCase(caseDetails.getId());
                CaseData caseData = caseDetailsConverter.toCaseData(detailsWithData);
                PaymentDetails hearingFeePaymentDetails = caseData.getHearingFeePaymentDetails();

                if (featureToggleService.isMintiEnabled()) {
                    if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
                        if (caseData.getHearingDueDate() == null) {
                            log.info("Current case status '{}'", caseDetails.getState());
                            applicationEventPublisher.publishEvent(new NoHearingFeeDueEvent(caseDetails.getId()));
                        } else {
                            if ((hearingFeePaymentDetails != null
                                && hearingFeePaymentDetails.getStatus() == PaymentStatus.SUCCESS)
                                && caseData.getHearingDueDate().isBefore(LocalDate.now())
                                || caseData.hearingFeePaymentDoneWithHWF()) {
                                log.info("new success");
                                applicationEventPublisher.publishEvent(new HearingFeePaidEvent(caseDetails.getId()));
                            } else if ((hearingFeePaymentDetails == null
                                || hearingFeePaymentDetails.getStatus() == PaymentStatus.FAILED)
                                && caseData.getHearingDueDate().isBefore(LocalDate.now())) {
                                log.info("new failed");
                                applicationEventPublisher.publishEvent(new HearingFeeUnpaidEvent(caseDetails.getId()));
                            }
                        }
                    } else {
                        preMultiIntermediateClaimLogic(caseDetails, hearingFeePaymentDetails, caseData);
                    }
                } else {
                    preMultiIntermediateClaimLogic(caseDetails, hearingFeePaymentDetails, caseData);
                }
            } catch (Exception e) {
                //Continue for other cases if there is some error in some cases, as we don't want
                // to stop processing other valid cases because error happened in some.
                //We log the error to leave a trace that something needs to be looked into for failed cases
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
    }

    private void preMultiIntermediateClaimLogic(CaseDetails caseDetails, PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        if ((hearingFeePaymentDetails != null
            && hearingFeePaymentDetails.getStatus() == PaymentStatus.SUCCESS)
            || caseData.hearingFeePaymentDoneWithHWF()) {
            log.info(" OLD SUCCESSS");
            applicationEventPublisher.publishEvent(new HearingFeePaidEvent(caseDetails.getId()));
        } else if (hearingFeePaymentDetails == null
            || hearingFeePaymentDetails.getStatus() == PaymentStatus.FAILED) {
            log.info("OLD failed");
            applicationEventPublisher.publishEvent(new HearingFeeUnpaidEvent(caseDetails.getId()));
        }
    }
}
