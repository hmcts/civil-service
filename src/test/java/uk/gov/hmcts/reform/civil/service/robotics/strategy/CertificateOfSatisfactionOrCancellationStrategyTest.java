package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.DebtPaymentOptions;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.DebtPaymentEvidence;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CERTIFICATE_OF_DEBT_PAYMENT;

class CertificateOfSatisfactionOrCancellationStrategyTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private CertificateOfSatisfactionOrCancellationStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new CertificateOfSatisfactionOrCancellationStrategy(featureToggleService, sequenceGenerator);
    }

    @Test
    void supportsReturnsFalseWhenToggleDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsEventWhenMarkedPaidByClaimant() {
        LocalDateTime issueDate = LocalDateTime.of(2024, 3, 10, 12, 0);
        LocalDate paidDate = LocalDate.of(2024, 3, 5);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc();
        caseData.setJoMarkedPaidInFullIssueDate(issueDate);
        caseData.setJoCoscRpaStatus(CoscRPAStatus.CANCELLED);
        caseData.setJoFullyPaymentMadeDate(paidDate);

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(7);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getCertificateOfSatisfactionOrCancellation()).hasSize(1);
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getEventSequence()).isEqualTo(7);
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getEventCode())
            .isEqualTo(EventType.CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION.getCode());
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.APPLICANT_ID);
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getEventDetails().getStatus())
            .isEqualTo(CoscRPAStatus.CANCELLED.toString());
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getEventDetails().getDatePaidInFull())
            .isEqualTo(paidDate);
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getDateReceived())
            .isEqualTo(issueDate);
    }

    @Test
    void contributeAddsEventWhenCoscCertificateApplied() {
        CaseDocument certificateDoc = new CaseDocument();
        certificateDoc.setDocumentType(CERTIFICATE_OF_DEBT_PAYMENT);
        DebtPaymentEvidence debtPaymentEvidence = new DebtPaymentEvidence();
        debtPaymentEvidence.setDebtPaymentOption(DebtPaymentOptions.MADE_FULL_PAYMENT_TO_COURT);
        CertOfSC certOfSC = new CertOfSC();
        LocalDate defendantPaidDate = LocalDate.of(2024, 3, 25);
        certOfSC.setDefendantFinalPaymentDate(defendantPaidDate);
        certOfSC.setDebtPaymentEvidence(debtPaymentEvidence);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc();
        Element<CaseDocument> certificateElement = ElementUtils.element(certificateDoc);
        caseData.setSystemGeneratedCaseDocuments(List.of(certificateElement));
        caseData.setCertOfSC(certOfSC);
        caseData.setJoMarkedPaidInFullIssueDate(null);
        LocalDateTime defendantIssueDate = LocalDateTime.of(2024, 4, 1, 16, 0);
        caseData.setJoDefendantMarkedPaidInFullIssueDate(defendantIssueDate);
        caseData.setJoCoscRpaStatus(CoscRPAStatus.SATISFIED);
        caseData.setJoFullyPaymentMadeDate(null);

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(12);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getCertificateOfSatisfactionOrCancellation()).hasSize(1);
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getEventDetails().getStatus())
            .isEqualTo(CoscRPAStatus.SATISFIED.toString());
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getEventDetails().getDatePaidInFull())
            .isEqualTo(defendantPaidDate);
        assertThat(history.getCertificateOfSatisfactionOrCancellation().get(0).getDateReceived())
            .isEqualTo(defendantIssueDate);
    }
}
