package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class JudgmentByAdmissionMapperTest {

    private RoboticsAddressMapper addressMapper;
    @Mock
    private FeatureToggleService featureToggleService;
    private InterestCalculator interestCalculator;
    private JudgementService judgementService;
    private JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;

    @Mock
    Time time;

    @BeforeEach
    void setUp() {
        addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());
        interestCalculator = new InterestCalculator();
        judgementService = new JudgementService(featureToggleService, interestCalculator);
        judgmentByAdmissionOnlineMapper =
            new JudgmentByAdmissionOnlineMapper(time, addressMapper, judgementService, interestCalculator);
    }

    @Test
    void testIfJudgmentByAdmission() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .totalInterest(BigDecimal.valueOf(10))
            .respondent1(PartyBuilder.builder().individual().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.ISSUED.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());

        assertEquals("Mr. John Rambo", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_IMMEDIATELY, caseData.getJoPaymentPlanSelected());

    }

    @Test
    void testIfJudgmentByAdmissionLrBulkAdmission() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .totalInterest(BigDecimal.valueOf(10))
            .respondent1(PartyBuilder.builder().individual().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());

    }

    @Test
    void testIfJudgmentByPartAdmissionLrPayImmediatelyNotAddInterest() {

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .totalInterest(BigDecimal.valueOf(10))
            .respondent1(PartyBuilder.builder().individual().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());

    }

    @Test
    void testIfJudgmentByFullAdmissionLrPayImmeidatelyNotAddInterest() {

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .totalInterest(BigDecimal.valueOf(10))
            .respondent1(PartyBuilder.builder().individual().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());

    }

    @ParameterizedTest
    @CsvSource({
        "ONCE_ONE_WEEK,WEEKLY",
        "ONCE_TWO_WEEKS,EVERY_TWO_WEEKS",
        "ONCE_ONE_MONTH,MONTHLY"
    })
    void testIfJudgmentByAdmission_scenario2(PaymentFrequencyLRspec paymentFrequencyLRspec,
                                             PaymentFrequency paymentFrequency) {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setFirstRepaymentDate(LocalDate.now().plusDays(10))
                                          .setPaymentAmount(new BigDecimal(1000))
                                          .setRepaymentFrequency(paymentFrequencyLRspec)
                                          )
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .respondent1(PartyBuilder.builder().individual().build())
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.ISSUED.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, activeJudgment.getPaymentPlan().getType());
        assertEquals("1000", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(paymentFrequency, activeJudgment.getInstalmentDetails().getPaymentFrequency());
        assertEquals(LocalDate.now().plusDays(10), activeJudgment.getInstalmentDetails().getStartDate());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());

        assertEquals("Mr. John Rambo", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, caseData.getJoPaymentPlanSelected());
        assertEquals("1000", caseData.getJoRepaymentAmount());
        assertNotNull(caseData.getJoRepaymentStartDate());
        assertEquals(paymentFrequency, caseData.getJoRepaymentFrequency());

    }

    @Test
    void testIfJudgmentByAdmission_scenario3() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.now().plusDays(5)))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .respondent1(PartyBuilder.builder().organisation().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.ISSUED.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, activeJudgment.getPaymentPlan().getType());
        assertEquals(activeJudgment.getPaymentPlan().getPaymentDeadlineDate(), LocalDate.now().plusDays(5));
        assertEquals(null, activeJudgment.getInstalmentDetails());
        assertEquals("The Organisation", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("The Organisation", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, caseData.getJoPaymentPlanSelected());
    }

    @Test
    void testIfJudgmentByAdmission_scenario4_multi_party() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.now().plusDays(5)))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .respondent1(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.REQUESTED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.NO, activeJudgment.getIsRegisterWithRTL());
        assertEquals(null, activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, activeJudgment.getPaymentPlan().getType());
        assertEquals(activeJudgment.getPaymentPlan().getPaymentDeadlineDate(), LocalDate.now().plusDays(5));
        assertEquals(null, activeJudgment.getInstalmentDetails());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant1Name());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant2Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant2Address());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant2Dob());

    }

    @Test
    void testIfJudgmentByAdmission_scenario5_courtDecisionInFavourClaimantLip() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.SET_DATE)
            .applicant1RequestedPaymentDateForDefendantSpec(new PaymentBySetDate()
                                                                .setPaymentSetDate(LocalDate.now().plusDays(5)))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                        .setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .respondent1(PartyBuilder.builder().organisation().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.ISSUED.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, activeJudgment.getPaymentPlan().getType());
        assertEquals(activeJudgment.getPaymentPlan().getPaymentDeadlineDate(), LocalDate.now().plusDays(5));
        assertEquals(null, activeJudgment.getInstalmentDetails());
        assertEquals("The Organisation", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("The Organisation", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, caseData.getJoPaymentPlanSelected());
    }

    @ParameterizedTest
    @CsvSource({
        "ONCE_ONE_WEEK,WEEKLY",
        "ONCE_TWO_WEEKS,EVERY_TWO_WEEKS",
        "ONCE_ONE_MONTH,MONTHLY"
    })
    void testIfJudgmentByAdmission_scenario6_courtDecisionInFavourClaimantLip(
        PaymentFrequencyClaimantResponseLRspec paymentFrequencyClaimantResponseLRspec, PaymentFrequency paymentFrequency) {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal(120))
            .totalClaimAmount(new BigDecimal(1000))
            .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(paymentFrequencyClaimantResponseLRspec)
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now().plusDays(10))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                        .setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .respondent1(PartyBuilder.builder().organisation().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);
        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.ISSUED.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, activeJudgment.getPaymentPlan().getType());
        assertEquals("120", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(paymentFrequency, activeJudgment.getInstalmentDetails().getPaymentFrequency());
        assertEquals(LocalDate.now().plusDays(10), activeJudgment.getInstalmentDetails().getStartDate());
        assertEquals(paymentFrequency, caseData.getJoRepaymentFrequency());
    }

    @ParameterizedTest
    @CsvSource({
        "120,120",
        "120.50,120.50",
        "120.49,120.49"
    })
    void testIfJudgmentByAdmission_scenario6_rounding(BigDecimal inputAmount, String expectedAmount) {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(inputAmount)
            .totalClaimAmount(new BigDecimal(1000))
            .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(PaymentFrequencyClaimantResponseLRspec.ONCE_ONE_WEEK)
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now().plusDays(10))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                        .setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .respondent1(PartyBuilder.builder().organisation().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);
        assertNotNull(activeJudgment);
        assertEquals(expectedAmount, activeJudgment.getInstalmentDetails().getAmount());
    }

    @Test
    void testIfJudgmentByAdmission_scenario7_courtDecisionInFavourClaimantLip() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal(10))
            .totalClaimAmount(new BigDecimal(1000))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                        .setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)))
            .ccjPaymentDetails(buildCCJPaymentDetails())
            .respondent1(PartyBuilder.builder().organisation().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);
        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.ISSUED.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(PaymentPlanSelection.PAY_IMMEDIATELY, activeJudgment.getPaymentPlan().getType());
    }

    @Test
    void testGetInstalmentDetails_ClaimantInFavour_NullAmount() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1(PartyBuilder.builder().individual().build())
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(null)
            .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(PaymentFrequencyClaimantResponseLRspec.ONCE_ONE_WEEK)
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now())
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                           .setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)))
            .build();

        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment.getInstalmentDetails());
        assertEquals(null, activeJudgment.getInstalmentDetails().getAmount());
    }

    @ParameterizedTest
    @CsvSource({
        "ONCE_ONE_WEEK,WEEKLY",
        "ONCE_TWO_WEEKS,EVERY_TWO_WEEKS",
        "ONCE_ONE_MONTH,MONTHLY"
    })
    void testGetInstalmentDetails_ClaimantInFavour_Frequencies(
        PaymentFrequencyClaimantResponseLRspec repFreq, PaymentFrequency expectedFreq) {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1(PartyBuilder.builder().individual().build())
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("100"))
            .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(repFreq)
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now())
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                           .setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)))
            .build();

        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment.getInstalmentDetails());
        assertEquals("100", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(expectedFreq, activeJudgment.getInstalmentDetails().getPaymentFrequency());
    }

    @Test
    void testGetInstalmentDetails_Respondent1Plan() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setPaymentAmount(new BigDecimal("200"))
                                          .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                          .setFirstRepaymentDate(LocalDate.now())
                                          )
            .build();

        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment.getInstalmentDetails());
        assertEquals("200", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(PaymentFrequency.MONTHLY, activeJudgment.getInstalmentDetails().getPaymentFrequency());
    }

    @Test
    void testGetInstalmentDetails_Respondent2Plan() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(null)
            .respondent2RepaymentPlan(new RepaymentPlanLRspec()
                                          .setPaymentAmount(new BigDecimal("300"))
                                          .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_TWO_WEEKS)
                                          .setFirstRepaymentDate(LocalDate.now())
                                          )
            .build();

        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment.getInstalmentDetails());
        assertEquals("300", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(PaymentFrequency.EVERY_TWO_WEEKS, activeJudgment.getInstalmentDetails().getPaymentFrequency());
    }

    @ParameterizedTest
    @CsvSource({
        "ONCE_ONE_WEEK,WEEKLY",
        "ONCE_TWO_WEEKS,EVERY_TWO_WEEKS",
        "ONCE_ONE_MONTH,MONTHLY"
    })
    void testGetInstalmentDetails_RespondentPlan_Frequencies(
        PaymentFrequencyLRspec repFreq, PaymentFrequency expectedFreq) {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setPaymentAmount(new BigDecimal("250"))
                                          .setRepaymentFrequency(repFreq)
                                          .setFirstRepaymentDate(LocalDate.now())
                                          )
            .build();

        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment.getInstalmentDetails());
        assertEquals("250", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(expectedFreq, activeJudgment.getInstalmentDetails().getPaymentFrequency());
    }

    private CCJPaymentDetails buildCCJPaymentDetails() {
        return new CCJPaymentDetails()
            .setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(140))
            .setCcjPaymentPaidSomeOption(YesOrNo.YES)
            .setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
            .setCcjJudgmentTotalStillOwed(BigDecimal.valueOf(150));
    }
}
