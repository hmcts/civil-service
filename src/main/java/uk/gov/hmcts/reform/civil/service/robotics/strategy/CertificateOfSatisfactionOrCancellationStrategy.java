package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

@Component
@Order(96)
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
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        boolean markPaidInFullExists = caseData.getJoMarkedPaidInFullIssueDate() != null;
        LocalDateTime dateReceived = markPaidInFullExists
            ? caseData.getJoMarkedPaidInFullIssueDate()
            : caseData.getJoDefendantMarkedPaidInFullIssueDate();

        EventDetails details = EventDetails.builder()
            .status(caseData.getJoCoscRpaStatus().toString())
            .datePaidInFull(resolvePaymentDate(caseData))
            .notificationReceiptDate(markPaidInFullExists
                ? caseData.getJoMarkedPaidInFullIssueDate().toLocalDate()
                : caseData.getJoDefendantMarkedPaidInFullIssueDate().toLocalDate())
            .build();

        Event event = Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION.getCode())
            .litigiousPartyID(markPaidInFullExists ? APPLICANT_ID : RESPONDENT_ID)
            .dateReceived(dateReceived)
            .eventDetails(details)
            .eventDetailsText("")
            .build();

        builder.certificateOfSatisfactionOrCancellation(event);
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
