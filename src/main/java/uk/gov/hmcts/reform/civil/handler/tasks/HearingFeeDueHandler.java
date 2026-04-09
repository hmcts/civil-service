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
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class HearingFeeDueHandler extends BaseExternalTaskHandler {

    private final HearingFeeDueSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                processCase(caseDetails);
            } catch (Exception e) {
                //Continue for other cases if there is some error in some cases, as we don't want
                // to stop processing other valid cases because error happened in some.
                //We log the error to leave a trace that something needs to be looked into for failed cases
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return new ExternalTaskData();
    }

    private void processCase(CaseDetails caseDetails) {
        CaseDetails detailsWithData = coreCaseDataService.getCase(caseDetails.getId());
        CaseData caseData = caseDetailsConverter.toCaseData(detailsWithData);
        PaymentDetails hearingFeePaymentDetails = caseData.getHearingFeePaymentDetails();

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            processMultiOrIntermediateCase(caseDetails, caseData, hearingFeePaymentDetails);
            return;
        }

        preMultiIntermediateClaimLogic(caseDetails, hearingFeePaymentDetails, caseData);
    }

    private void processMultiOrIntermediateCase(
        CaseDetails caseDetails,
        CaseData caseData,
        PaymentDetails hearingFeePaymentDetails
    ) {
        if (caseData.getHearingDueDate() == null) {
            publishNoHearingFeeDueEvent(caseDetails);
            return;
        }

        publishFeeEvents(caseDetails, hearingFeePaymentDetails, caseData, false);
    }

    private void preMultiIntermediateClaimLogic(CaseDetails caseDetails, PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        publishFeeEvents(caseDetails, hearingFeePaymentDetails, caseData, true);
    }

    private void publishFeeEvents(
        CaseDetails caseDetails,
        PaymentDetails hearingFeePaymentDetails,
        CaseData caseData,
        boolean isPreMultiIntermediate
    ) {
        if (isHearingFeePaid(hearingFeePaymentDetails, caseData)) {
            publishHearingFeePaidEvent(caseDetails, isPreMultiIntermediate);
        } else if (isHearingFeeUnpaid(hearingFeePaymentDetails, caseData)) {
            publishHearingFeeUnpaidEvent(caseDetails, isPreMultiIntermediate);
        }
    }

    private boolean isHearingFeePaid(PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        return isSuccessfulPaymentBeforeDueDate(hearingFeePaymentDetails, caseData) || caseData.hearingFeePaymentDoneWithHWF();
    }

    private boolean isSuccessfulPaymentBeforeDueDate(PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        return hearingFeePaymentDetails != null
            && hearingFeePaymentDetails.getStatus() == PaymentStatus.SUCCESS
            && caseData.getHearingDueDate().isBefore(LocalDate.now());
    }

    private boolean isHearingFeeUnpaid(PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        return (hearingFeePaymentDetails == null || hearingFeePaymentDetails.getStatus() == PaymentStatus.FAILED)
            && caseData.getHearingDueDate().isBefore(LocalDate.now());
    }

    private void publishNoHearingFeeDueEvent(CaseDetails caseDetails) {
        log.info("Publishing NoHearingFeeDueEvent current case status {}, Case Id {}",
                 caseDetails.getState(), caseDetails.getId());
        applicationEventPublisher.publishEvent(new NoHearingFeeDueEvent(caseDetails.getId()));
    }

    private void publishHearingFeePaidEvent(CaseDetails caseDetails, boolean isPreMultiIntermediate) {
        if (log.isInfoEnabled()) {
            log.info(
                "{}HearingFeePaidEvent current case status {}, Case Id {}",
                isPreMultiIntermediate ? "preMultiIntermediateClaimLogic publishing " : "Publishing ",
                caseDetails.getState(),
                caseDetails.getId()
            );
        }
        applicationEventPublisher.publishEvent(new HearingFeePaidEvent(caseDetails.getId()));
    }

    private void publishHearingFeeUnpaidEvent(CaseDetails caseDetails, boolean isPreMultiIntermediate) {
        if (log.isInfoEnabled()) {
            log.info(
                "{}HearingFeeUnpaidEvent current case status {}, Case Id {}",
                isPreMultiIntermediate ? "preMultiIntermediateClaimLogic publishing " : "Publishing ",
                caseDetails.getState(),
                caseDetails.getId()
            );
        }
        applicationEventPublisher.publishEvent(new HearingFeeUnpaidEvent(caseDetails.getId()));
    }
}
