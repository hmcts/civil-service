package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.support.StrategyTestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFAULT_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;

class DefaultJudgmentEventStrategyTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsPartyLookup partyLookup;

    private DefaultJudgmentEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new DefaultJudgmentEventStrategy(
            featureToggleService,
            timelineHelper,
            sequenceGenerator,
            partyLookup,
            new RoboticsEventTextFormatter()
        );
    }

    @Test
    void supportsReturnsFalseWhenDefendantDetailsMissing() {
        assertThat(strategy.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenDefendantDetailsPresent() {
        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Case();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributesDefaultJudgmentEventsAndMisc() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        LocalDateTime now = baseDate.atTime(9, 0);
        when(timelineHelper.now()).thenReturn(now);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Case();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefaultJudgment()).hasSize(1);
        assertThat(history.getDefaultJudgment().get(0).getEventCode())
            .isEqualTo(DEFAULT_JUDGMENT_GRANTED.getCode());
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventCode()).isEqualTo(MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Default Judgment granted and claim moved offline.");
        assertThat(history.getDefaultJudgment().get(0).getEventDetails().getInstallmentAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void grantedFlagEmitsRequestedMessageOnly() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1);
        when(timelineHelper.now()).thenReturn(baseDate.atTime(10, 0));

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v2Case();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefaultJudgment()).isNullOrEmpty();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Default Judgment requested and claim moved offline.");
    }

    @Test
    void joLiveFeedUsesRecordJudgmentMessage() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(baseDate.atTime(12, 0));
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Case();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("Judgment recorded.");
    }

    @Test
    void createsDefaultJudgmentEventsForBothRespondentsWhenBothSelected() {
        LocalDateTime now = LocalDate.of(2024, 2, 10).atTime(15, 30);
        when(timelineHelper.now()).thenReturn(now);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2, 3);
        when(partyLookup.respondentId(0)).thenReturn("002");
        when(partyLookup.respondentId(1)).thenReturn("003");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v2Builder()
            .defendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("Both defendants")
                    .build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefaultJudgment()).hasSize(2);
        assertThat(history.getDefaultJudgment().get(0).getLitigiousPartyID()).isEqualTo("002");
        assertThat(history.getDefaultJudgment().get(1).getLitigiousPartyID()).isEqualTo("003");
        history.getDefaultJudgment().forEach(event -> assertThat(event.getDateReceived()).isEqualTo(now));
        assertThat(history.getMiscellaneous())
            .singleElement()
            .satisfies(event ->
                assertThat(event.getEventDetailsText())
                    .isEqualTo("RPA Reason: Default Judgment granted and claim moved offline.")
            );
    }

    @Test
    void populatesInstallmentDetailsFromClaimantPlanWhenLipEnabled() {
        LocalDateTime now = LocalDate.of(2024, 3, 5).atTime(9, 15);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .applicant1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                    .build())
                .build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("1234"))
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.of(2024, 4, 1))
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal("5500")).build())
            .totalInterest(BigDecimal.valueOf(150))
            .repaymentSuggestion("1234")
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        EventDetails details = history.getDefaultJudgment().get(0).getEventDetails();
        assertThat(details.getInstallmentAmount()).isEqualByComparingTo("12.34");
        assertThat(details.getInstallmentPeriod()).isEqualTo("FOR");
        assertThat(details.getAmountOfCosts()).isEqualByComparingTo("55.00");
        assertThat(details.getPaymentInFullDate()).isNull();
    }

    @Test
    void usesClaimantRequestedSetDateWhenCourtDecisionSupports() {
        LocalDate requestedPaymentDate = LocalDate.of(2024, 6, 20);
        LocalDateTime now = LocalDate.of(2024, 4, 18).atTime(11, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                    .build())
                .build())
            .paymentTypeSelection(DJPaymentTypeSelection.SET_DATE)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .applicant1RequestedPaymentDateForDefendantSpec(
                PaymentBySetDate.builder().paymentSetDate(requestedPaymentDate).build()
            )
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.build().getDefaultJudgment().get(0).getEventDetails();
        assertThat(details.getPaymentInFullDate()).isEqualTo(requestedPaymentDate.atStartOfDay());
        assertThat(details.getInstallmentPeriod()).isEqualTo("FUL");
    }

    @Test
    void usesApplicantImmediateDateWhenCourtDecisionSupports() {
        LocalDate immediateDate = LocalDate.of(2024, 7, 10);
        LocalDateTime now = LocalDate.of(2024, 7, 1).atTime(8, 30);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                    .build())
                .build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec(immediateDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.build().getDefaultJudgment().get(0).getEventDetails();
        assertThat(details.getPaymentInFullDate()).isEqualTo(immediateDate.atStartOfDay());
    }

    @Test
    void usesRespondentRepaymentPlanWhenCourtDecisionNotInFavour() {
        LocalDateTime now = LocalDate.of(2024, 8, 1).atTime(9, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                .paymentAmount(new BigDecimal("4321"))
                .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                .firstRepaymentDate(LocalDate.of(2024, 8, 15))
                .build())
            .repaymentSuggestion("4321")
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.build().getDefaultJudgment().get(0).getEventDetails();
        assertThat(details.getInstallmentAmount()).isEqualByComparingTo("43.21");
        assertThat(details.getInstallmentPeriod()).isEqualTo("WK");
    }

    @Test
    void fallsBackToRepaymentSuggestionWhenNoPlanProvided() {
        LocalDateTime now = LocalDate.of(2024, 10, 1).atTime(9, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .repaymentSuggestion("1234")
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.build().getDefaultJudgment().get(0).getEventDetails();
        assertThat(details.getInstallmentAmount()).isEqualByComparingTo("12.34");
        assertThat(details.getInstallmentPeriod()).isEqualTo("MTH");
    }

    @Test
    void usesZeroInstallmentAmountWhenNotRepaymentPlan() {
        LocalDateTime now = LocalDate.of(2024, 11, 1).atTime(9, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.build().getDefaultJudgment().get(0).getEventDetails();
        assertThat(details.getInstallmentAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void mapsMonthlyRepaymentFrequencyToMth() {
        LocalDateTime now = LocalDate.of(2024, 9, 1).atTime(10, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                    .build())
                .build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("2000"))
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.of(2024, 9, 15))
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.build().getDefaultJudgment().get(0).getEventDetails();
        assertThat(details.getInstallmentPeriod()).isEqualTo("MTH");
    }

    @Test
    void usesJoCreatedDateWhenFeedActiveAndDatePresent() {
        LocalDateTime jolDate = LocalDateTime.of(2024, 10, 5, 14, 45);
        LocalDateTime now = LocalDateTime.of(2024, 10, 6, 9, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .joDJCreatedDate(jolDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        EventDetails details = history.getDefaultJudgment().get(0).getEventDetails();
        assertThat(history.getDefaultJudgment().get(0).getDateReceived()).isEqualTo(jolDate);
        assertThat(details.getDateOfJudgment()).isEqualTo(jolDate);
    }
}
