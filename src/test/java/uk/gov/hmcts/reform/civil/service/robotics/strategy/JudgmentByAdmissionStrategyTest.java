package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
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

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(1000));
        ccjPaymentDetails.setCcjJudgmentAmountClaimFee(BigDecimal.valueOf(100));
        ccjPaymentDetails.setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(50));
        ccjPaymentDetails.setCcjPaymentPaidSomeAmountInPounds(BigDecimal.valueOf(10));

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        caseData.setApplicant1ResponseDate(responseDate);
        caseData.setJoJudgementByAdmissionIssueDate(null);

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
    void contributeCalculatesJudgmentAmountWithInterest() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        LocalDateTime responseDate = LocalDateTime.of(2024, 1, 1, 10, 0);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(1000));
        ccjPaymentDetails.setCcjPaymentPaidSomeAmountInPounds(BigDecimal.ZERO);

        CaseData caseData = CaseDataBuilder.builder()
            .ccjPaymentDetails(ccjPaymentDetails)
            .totalInterest(BigDecimal.valueOf(200))
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1ResponseDate(responseDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getJudgmentByAdmission()).hasSize(1);
        assertThat(history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfJudgment())
            .isEqualByComparingTo("1200.00");
    }

    @Test
    void contributeCalculatesJudgmentAmountForLipVLip() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(3, 4);
        LocalDateTime responseDate = LocalDateTime.of(2024, 1, 2, 10, 0);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(1000));
        ccjPaymentDetails.setCcjJudgmentLipInterest(BigDecimal.valueOf(150));
        ccjPaymentDetails.setCcjPaymentPaidSomeAmountInPounds(BigDecimal.ZERO);

        CaseData caseData = CaseDataBuilder.builder()
            .ccjPaymentDetails(ccjPaymentDetails)
            .totalInterest(BigDecimal.valueOf(200))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1ResponseDate(responseDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getJudgmentByAdmission()).hasSize(1);
        assertThat(history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfJudgment())
            .isEqualByComparingTo("1150.00");
    }

    @Test
    void contributeCalculatesJudgmentAmountForPartAdmitLipVLipWithoutLipInterest() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(5, 6);
        LocalDateTime responseDate = LocalDateTime.of(2024, 1, 3, 10, 0);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(1000));
        ccjPaymentDetails.setCcjJudgmentLipInterest(BigDecimal.valueOf(150));
        ccjPaymentDetails.setCcjPaymentPaidSomeAmountInPounds(BigDecimal.ZERO);

        CaseData caseData = CaseDataBuilder.builder()
            .ccjPaymentDetails(ccjPaymentDetails)
            .totalInterest(BigDecimal.ZERO)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1ResponseDate(responseDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getJudgmentByAdmission()).hasSize(1);
        assertThat(history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfJudgment())
            .isEqualByComparingTo("1000.00");
    }

    @Test
    void contributeUsesJoIssueDateWhenLiveFeedActive() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(800));
        ccjPaymentDetails.setCcjJudgmentAmountClaimFee(BigDecimal.valueOf(120));
        ccjPaymentDetails.setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(40));
        ccjPaymentDetails.setCcjPaymentPaidSomeAmountInPounds(BigDecimal.ZERO);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately();
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        LocalDateTime issueDate = LocalDateTime.of(2024, 3, 5, 15, 30);
        caseData.setJoJudgementByAdmissionIssueDate(issueDate);

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

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        Fee claimFee = new Fee();
        claimFee.setCalculatedAmountInPence(new BigDecimal("9900"));
        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(500));
        ccjPaymentDetails.setCcjJudgmentLipInterest(BigDecimal.valueOf(45));
        ccjPaymentDetails.setCcjJudgmentAmountClaimFee(BigDecimal.valueOf(80));
        ccjPaymentDetails.setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(50));
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);
        ccjPaymentDetails.setCcjPaymentPaidSomeAmountInPounds(BigDecimal.valueOf(5));

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setApplicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN);
        caseData.setApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("2500"));
        caseData.setApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(
            PaymentFrequencyClaimantResponseLRspec.ONCE_ONE_MONTH);
        caseData.setApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.of(2024, 6, 15));
        caseData.setClaimFee(claimFee);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        caseData.setApplicant1ResponseDate(responseDate);

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

        LocalDate paymentDate = LocalDate.of(2024, 8, 20);

        RespondToClaimAdmitPartLRspec respondToClaim = new RespondToClaimAdmitPartLRspec();
        respondToClaim.setWhenWillThisAmountBePaid(paymentDate);
        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(900));
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately();
        LocalDateTime issueDate = LocalDateTime.of(2024, 7, 10, 14, 0);
        caseData.setJoJudgementByAdmissionIssueDate(issueDate);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaim);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);

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
    void includesInterestForLrClaims() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(61, 62);

        LocalDateTime responseDate = LocalDateTime.of(2024, 11, 5, 10, 0);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(new BigDecimal("1000"));
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setTotalInterest(new BigDecimal("250"));
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        caseData.setJoJudgementByAdmissionIssueDate(null);
        caseData.setApplicant1ResponseDate(responseDate);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getJudgmentByAdmission()).hasSize(1);
        BigDecimal amount = history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfJudgment();
        assertThat(amount).isEqualByComparingTo(new BigDecimal("1250.00"));
    }

    @Test
    void returnsFwCodeWhenJoActiveAndImmediatePayment() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(51, 52);

        RepaymentPlanLRspec repaymentPlan = new RepaymentPlanLRspec();
        repaymentPlan.setPaymentAmount(new BigDecimal("4321"));
        repaymentPlan.setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_TWO_WEEKS);
        repaymentPlan.setFirstRepaymentDate(LocalDate.of(2024, 9, 15));
        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(750));
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately();
        LocalDateTime issueDate = LocalDateTime.of(2024, 9, 1, 11, 30);
        caseData.setJoJudgementByAdmissionIssueDate(issueDate);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setRespondent1RepaymentPlan(repaymentPlan);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        var details = history.getJudgmentByAdmission().get(0).getEventDetails();
        assertThat(details.getInstallmentPeriod()).isEqualTo("FW");
        assertThat(details.getPaymentInFullDate()).isNull();
    }
}
