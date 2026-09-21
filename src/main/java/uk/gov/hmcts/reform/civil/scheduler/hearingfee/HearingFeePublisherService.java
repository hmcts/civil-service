package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.TaskAbortedException;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingFeePublisherService {

    private static final String DEFAULT_PREFIX = "Publishing ";
    private static final String PRE_MULTI_PREFIX = "preMultiIntermediateClaimLogic publishing ";
    private static final String NO_HEARING_FEE_DUE_EVENT = "NoHearingFeeDueEvent";
    private static final String HEARING_FEE_PAID_EVENT = "HearingFeePaidEvent";
    private static final String HEARING_FEE_UNPAID_EVENT = "HearingFeeUnpaidEvent";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final FeatureToggleService featureToggleService;
    private final Time time;

    public void publishHearingFeeEvent(CaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        boolean isMultiOrIntermediateTrack = featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        PaymentDetails paymentDetails = caseData.getHearingFeePaymentDetails();
        String prefix = isMultiOrIntermediateTrack ? DEFAULT_PREFIX : PRE_MULTI_PREFIX;

        if (isMultiOrIntermediateTrack && caseData.getHearingDueDate() == null) {
            publishEvent(prefix, NO_HEARING_FEE_DUE_EVENT, caseData, new NoHearingFeeDueEvent(caseId));
        } else if (isHearingFeePaid(paymentDetails, caseData)) {
            publishEvent(prefix, HEARING_FEE_PAID_EVENT, caseData, new HearingFeePaidEvent(caseId));
        } else if (isHearingFeeUnpaid(paymentDetails, caseData)) {
            publishEvent(prefix, HEARING_FEE_UNPAID_EVENT, caseData, new HearingFeeUnpaidEvent(caseId));
        } else {
            throw new TaskAbortedException("No condition matched (e.g. hearing due date not yet reached)");
        }
    }

    private boolean isHearingFeePaid(PaymentDetails paymentDetails, CaseData caseData) {
        return isSuccessfulPaymentWithDueDatePassed(paymentDetails, caseData)
            || caseData.hearingFeePaymentDoneWithHWF();
    }

    private boolean isHearingFeeUnpaid(PaymentDetails paymentDetails, CaseData caseData) {
        return (paymentDetails == null || paymentDetails.getStatus() == PaymentStatus.FAILED)
            && isHearingDueDatePassed(caseData.getHearingDueDate());
    }

    private boolean isSuccessfulPaymentWithDueDatePassed(PaymentDetails paymentDetails, CaseData caseData) {
        return paymentDetails != null
            && paymentDetails.getStatus() == PaymentStatus.SUCCESS
            && isHearingDueDatePassed(caseData.getHearingDueDate());
    }

    private boolean isHearingDueDatePassed(LocalDate hearingDueDate) {
        return hearingDueDate != null && hearingDueDate.isBefore(time.now().toLocalDate());
    }

    private void publishEvent(String prefix, String eventName, CaseData caseData, Object event) {
        log.info(
            "{}{} current case status {}, Case Id {}",
            prefix,
            eventName,
            caseData.getCcdState() != null ? caseData.getCcdState().name() : "null",
            caseData.getCcdCaseReference()
        );
        applicationEventPublisher.publishEvent(event);
    }
}
