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
        CaseData caseData = CaseData.builder().build();
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsEventWhenMarkedPaidByClaimant() {
        LocalDateTime issueDate = LocalDateTime.of(2024, 3, 10, 12, 0);
        LocalDate paidDate = LocalDate.of(2024, 3, 5);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc()
            .toBuilder()
            .joMarkedPaidInFullIssueDate(issueDate)
            .joCoscRpaStatus(CoscRPAStatus.CANCELLED)
            .joFullyPaymentMadeDate(paidDate)
            .build();

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
        LocalDateTime defendantIssueDate = LocalDateTime.of(2024, 4, 1, 16, 0);
        LocalDate defendantPaidDate = LocalDate.of(2024, 3, 25);

        CaseDocument certificateDoc = CaseDocument.builder()
            .documentType(CERTIFICATE_OF_DEBT_PAYMENT)
            .build();
        Element<CaseDocument> certificateElement = ElementUtils.element(certificateDoc);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc()
            .toBuilder()
            .systemGeneratedCaseDocuments(List.of(certificateElement))
            .certOfSC(CertOfSC.builder()
                .defendantFinalPaymentDate(defendantPaidDate)
                .debtPaymentEvidence(DebtPaymentEvidence.builder()
                    .debtPaymentOption(DebtPaymentOptions.MADE_FULL_PAYMENT_TO_COURT).build())
                .build())
            .joMarkedPaidInFullIssueDate(null)
            .joDefendantMarkedPaidInFullIssueDate(defendantIssueDate)
            .joCoscRpaStatus(CoscRPAStatus.SATISFIED)
            .joFullyPaymentMadeDate(null)
            .build();

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
