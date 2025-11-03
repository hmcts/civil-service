package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JudgmentByAdmissionStrategyTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private RoboticsTimelineHelper timelineHelper;

    private JudgmentByAdmissionStrategy strategy;

    private RoboticsEventTextFormatter textFormatter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        textFormatter = new RoboticsEventTextFormatter();
        strategy = new JudgmentByAdmissionStrategy(
            featureToggleService,
            textFormatter,
            sequenceGenerator,
            timelineHelper
        );
    }

    @Test
    void supportsReturnsFalseWhenRequestNotMade() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsOfflineJudgmentEvents() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);
        LocalDateTime responseDate = LocalDateTime.of(2024, 2, 1, 10, 0);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder()
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1000))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(100))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(50))
                .ccjPaymentPaidSomeAmountInPounds(BigDecimal.valueOf(10))
                .build())
            .applicant1ResponseDate(responseDate)
            .joJudgementByAdmissionIssueDate(null)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getMiscellaneous().get(0).getEventCode())
            .isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(textFormatter.judgmentByAdmissionOffline());

        assertThat(history.getJudgmentByAdmission()).hasSize(1);
        assertThat(history.getJudgmentByAdmission().get(0).getEventSequence()).isEqualTo(11);
        assertThat(history.getJudgmentByAdmission().get(0).getEventCode())
            .isEqualTo(EventType.JUDGEMENT_BY_ADMISSION.getCode());
        assertThat(history.getJudgmentByAdmission().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.APPLICANT_ID);
        assertThat(history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfJudgment())
            .isEqualByComparingTo(BigDecimal.valueOf(1000).setScale(2));
        assertThat(history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfCosts())
            .isEqualByComparingTo(BigDecimal.valueOf(150).setScale(2));
    }

    @Test
    void contributeUsesJoIssueDateWhenLiveFeedActive() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        LocalDateTime issueDate = LocalDateTime.of(2024, 3, 5, 15, 30);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately()
            .toBuilder()
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(800))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(120))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(BigDecimal.ZERO)
                .build())
            .joJudgementByAdmissionIssueDate(issueDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetails().getMiscText())
            .isEqualTo(EventHistoryMapper.RECORD_JUDGMENT);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(issueDate);
        assertThat(history.getJudgmentByAdmission().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
    }

    @Test
    void populatesClaimantInstalmentDetailsWhenLipEnabled() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(31, 32);
        LocalDateTime responseDate = LocalDateTime.of(2024, 5, 1, 9, 0);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                    .build())
                .build())
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("2500"))
            .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(
                PaymentFrequencyClaimantResponseLRspec.ONCE_ONE_MONTH)
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.of(2024, 6, 15))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal("9900")).build())
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(500))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(45))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(80))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(50))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmountInPounds(BigDecimal.valueOf(5))
                .build())
            .applicant1ResponseDate(responseDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getJudgmentByAdmission()).hasSize(1);
        var details = history.getJudgmentByAdmission().get(0).getEventDetails();
        assertThat(details.getAmountOfCosts()).isEqualByComparingTo("99.00");
        assertThat(details.getAmountOfJudgment()).isEqualByComparingTo("545.00");
        assertThat(details.getInstallmentAmount()).isEqualByComparingTo("25.00");
        assertThat(details.getInstallmentPeriod()).isEqualTo("MTH");
        assertThat(details.getFirstInstallmentDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(details.getAmountPaidBeforeJudgment()).isEqualByComparingTo("5.00");
        assertThat(details.getPaymentInFullDate()).isNull();
        assertThat(history.getJudgmentByAdmission().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.APPLICANT_ID);
    }

    @Test
    void usesDefendantSetDateWhenJoActive() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(41, 42);

        LocalDateTime issueDate = LocalDateTime.of(2024, 7, 10, 14, 0);
        LocalDate paymentDate = LocalDate.of(2024, 8, 20);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately()
            .toBuilder()
            .joJudgementByAdmissionIssueDate(issueDate)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(paymentDate)
                .build())
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(900))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        var details = history.getJudgmentByAdmission().get(0).getEventDetails();
        assertThat(details.getPaymentInFullDate()).isEqualTo(paymentDate.atStartOfDay());
        assertThat(details.getInstallmentPeriod()).isEqualTo("FUL");
        assertThat(history.getJudgmentByAdmission().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getMiscellaneous()).singleElement()
            .extracting(event -> event.getEventDetails().getMiscText())
            .isEqualTo(EventHistoryMapper.RECORD_JUDGMENT);
    }

    @Test
    void returnsFwCodeWhenJoActiveAndImmediatePayment() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(51, 52);

        LocalDateTime issueDate = LocalDateTime.of(2024, 9, 1, 11, 30);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately()
            .toBuilder()
            .joJudgementByAdmissionIssueDate(issueDate)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                .paymentAmount(new BigDecimal("4321"))
                .repaymentFrequency(PaymentFrequencyLRspec.ONCE_TWO_WEEKS)
                .firstRepaymentDate(LocalDate.of(2024, 9, 15))
                .build())
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(750))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        var details = history.getJudgmentByAdmission().get(0).getEventDetails();
        assertThat(details.getInstallmentPeriod()).isEqualTo("FW");
        assertThat(details.getPaymentInFullDate()).isNull();
    }
}
