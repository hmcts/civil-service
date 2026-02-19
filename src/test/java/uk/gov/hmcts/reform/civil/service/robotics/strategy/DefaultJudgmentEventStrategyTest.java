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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.StrategyTestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
        assertThat(strategy.supports(CaseDataBuilder.builder().build())).isFalse();
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefaultJudgment()).hasSize(1);
        assertThat(builder.getDefaultJudgment().getFirst().getEventCode())
            .isEqualTo(DEFAULT_JUDGMENT_GRANTED.getCode());
        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventCode()).isEqualTo(MISCELLANEOUS.getCode());
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("RPA Reason: Default Judgment granted and claim moved offline.");
        assertThat(builder.getDefaultJudgment().getFirst().getEventDetails().getInstallmentAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void grantedFlagEmitsRequestedMessageOnly() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1);
        when(timelineHelper.now()).thenReturn(baseDate.atTime(10, 0));

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v2Case();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefaultJudgment()).isNullOrEmpty();
        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("Judgment recorded.");
    }

    @Test
    void createsDefaultJudgmentEventsForBothRespondentsWhenBothSelected() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2, 3);
        when(partyLookup.respondentId(0)).thenReturn("002");
        when(partyLookup.respondentId(1)).thenReturn("003");

        DynamicListElement element = new DynamicListElement();
        element.setCode(UUID.randomUUID().toString());
        element.setLabel("Both defendants");
        DynamicList defendantDetailsSpec = new DynamicList();
        defendantDetailsSpec.setValue(element);

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v2Builder()
            .defendantDetailsSpec(defendantDetailsSpec)
            .build();

        EventHistory builder = new EventHistory();
        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(builder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        builder.getDefaultJudgment().forEach(event -> {
            assertThat(event.getDateReceived()).isAfterOrEqualTo(before);
            assertThat(event.getDateReceived()).isBeforeOrEqualTo(after);
        });
        assertThat(builder.getDefaultJudgment()).hasSize(2);
        assertThat(builder.getDefaultJudgment().getFirst().getLitigiousPartyID()).isEqualTo("002");
        assertThat(builder.getDefaultJudgment().get(1).getLitigiousPartyID()).isEqualTo("003");
        assertThat(builder.getMiscellaneous())
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

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        Fee claimFee = new Fee();
        claimFee.setCalculatedAmountInPence(new BigDecimal("5500"));

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .applicant1Represented(YesOrNo.NO)
            .caseDataLiP(caseDataLiP)
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("1234"))
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.of(2024, 4, 1))
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)
            .claimFee(claimFee)
            .totalInterest(BigDecimal.valueOf(150))
            .repaymentSuggestion("1234")
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
        assertThat(details.getInstallmentAmount()).isEqualByComparingTo("12.34");
        assertThat(details.getInstallmentPeriod()).isEqualTo("FOR");
        assertThat(details.getAmountOfCosts()).isEqualByComparingTo("55.00");
        assertThat(details.getPaymentInFullDate()).isNull();
    }

    @Test
    void usesClaimantRequestedSetDateWhenCourtDecisionSupports() {
        LocalDateTime now = LocalDate.of(2024, 4, 18).atTime(11, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        PaymentBySetDate paymentBySetDate = new PaymentBySetDate();
        LocalDate requestedPaymentDate = LocalDate.of(2024, 6, 20);
        paymentBySetDate.setPaymentSetDate(requestedPaymentDate);

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .caseDataLiP(caseDataLiP)
            .paymentTypeSelection(DJPaymentTypeSelection.SET_DATE)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .applicant1RequestedPaymentDateForDefendantSpec(paymentBySetDate)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
        assertThat(details.getPaymentInFullDate()).isEqualTo(requestedPaymentDate.atStartOfDay());
        assertThat(details.getInstallmentPeriod()).isEqualTo("FUL");
    }

    @Test
    void usesApplicantImmediateDateWhenCourtDecisionSupports() {
        LocalDateTime now = LocalDate.of(2024, 7, 1).atTime(8, 30);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        LocalDate immediateDate = LocalDate.of(2024, 7, 10);
        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .caseDataLiP(caseDataLiP)
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec(immediateDate)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
        assertThat(details.getPaymentInFullDate()).isEqualTo(immediateDate.atStartOfDay());
    }

    @Test
    void usesRespondentRepaymentPlanWhenCourtDecisionNotInFavour() {
        LocalDateTime now = LocalDate.of(2024, 8, 1).atTime(9, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        lenient().when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        RepaymentPlanLRspec repaymentPlan = new RepaymentPlanLRspec();
        repaymentPlan.setPaymentAmount(new BigDecimal("4321"));
        repaymentPlan.setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH);
        repaymentPlan.setFirstRepaymentDate(LocalDate.of(2024, 8, 15));

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(repaymentPlan)
            .repaymentSuggestion("4321")
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
        assertThat(details.getInstallmentAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void mapsMonthlyRepaymentFrequencyToMth() {
        LocalDateTime now = LocalDate.of(2024, 9, 1).atTime(10, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(timelineHelper.now()).thenReturn(now);
        when(partyLookup.respondentId(0)).thenReturn("002");

        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);

        CaseData caseData = StrategyTestDataFactory.defaultJudgment1v1Builder()
            .caseDataLiP(caseDataLiP)
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("2000"))
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.of(2024, 9, 15))
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .repaymentSuggestion("2000")
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        EventDetails details = builder.getDefaultJudgment().getFirst().getEventDetails();
        assertThat(builder.getDefaultJudgment().getFirst().getDateReceived()).isEqualTo(jolDate);
        assertThat(details.getDateOfJudgment()).isEqualTo(jolDate);
    }
}
