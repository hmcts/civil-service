package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateOfSatisfactionOrCancellationStrategy implements EventHistoryStrategy {

    private final FeatureToggleService featureToggleService;
    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null || !featureToggleService.isJOLiveFeedActive()) {
            return false;
        }
        boolean markPaidInFullExists = caseData.getJoMarkedPaidInFullIssueDate() != null;
        boolean coscApplied = caseData.hasCoscCert();
        return (markPaidInFullExists && caseData.getJoDefendantMarkedPaidInFullIssueDate() == null)
                || coscApplied;
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building COSC robotics events for caseId {}", caseData.getCcdCaseReference());

        boolean markPaidInFullExists = caseData.getJoMarkedPaidInFullIssueDate() != null;
        LocalDateTime dateReceived =
                markPaidInFullExists
                        ? caseData.getJoMarkedPaidInFullIssueDate()
                        : caseData.getJoDefendantMarkedPaidInFullIssueDate();

        EventDetails details =
                new EventDetails()
                        .setStatus(caseData.getJoCoscRpaStatus().toString())
                        .setDatePaidInFull(resolvePaymentDate(caseData))
                        .setNotificationReceiptDate(
                                markPaidInFullExists
                                        ? caseData.getJoMarkedPaidInFullIssueDate().toLocalDate()
                                        : caseData.getJoDefendantMarkedPaidInFullIssueDate().toLocalDate());

        Event event =
                createEvent(
                        sequenceGenerator.nextSequence(eventHistory),
                        EventType.CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION.getCode(),
                        dateReceived,
                        markPaidInFullExists ? APPLICANT_ID : RESPONDENT_ID,
                        "",
                        details);

        List<Event> updatedCertificateOfSatisfactionOrCancellationEvents1 =
                eventHistory.getCertificateOfSatisfactionOrCancellation() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getCertificateOfSatisfactionOrCancellation());
        updatedCertificateOfSatisfactionOrCancellationEvents1.add(event);
        eventHistory.setCertificateOfSatisfactionOrCancellation(
                updatedCertificateOfSatisfactionOrCancellationEvents1);
    }

    private LocalDate resolvePaymentDate(CaseData caseData) {
        if (caseData.getJoFullyPaymentMadeDate() != null) {
            return caseData.getJoFullyPaymentMadeDate();
        }
        CertOfSC certOfSC = caseData.getCertOfSC();
        if (certOfSC != null && certOfSC.getDefendantFinalPaymentDate() != null) {
            return certOfSC.getDefendantFinalPaymentDate();
        }
        throw new IllegalArgumentException("Payment date cannot be null");
    }
}
