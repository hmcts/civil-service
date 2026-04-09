package uk.gov.hmcts.reform.civil.model.docmosis;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionCalculator;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;

@ExtendWith(MockitoExtension.class)
public class InterlocutoryJudgementDocMapperTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2023, 11, 28);
    private static final String CLAIM_NUMBER = "claim001";
    private static final String REFER_TO_JUDGE = "Refer to Judge";
    private static final LocalDateTime CLAIMANT_RESPONSE_DATE_TIME = LocalDateTime.of(2023, 11, 28, 6, 30, 40, 50000);

    @Mock
    private DeadlineExtensionCalculatorService calculatorService;
    @Mock
    private RepaymentPlanDecisionCalculator repaymentPlanDecisionCalculator;
    @Mock
    private ClaimantResponseUtils claimantResponseUtils;
    private InterlocutoryJudgementDocMapper mapper;
    private CaseData caseData;

    @BeforeEach
    public void setup() {
        mapper = new InterlocutoryJudgementDocMapper(calculatorService, repaymentPlanDecisionCalculator, claimantResponseUtils);
    }

    @Test
    void shouldMapCaseDataToInterlocutoryJudgementDoc() {

        //Given
        caseData = getCaseData();
        try (MockedStatic mocked = mockStatic(ClaimantResponseUtils.class)) {
            given(repaymentPlanDecisionCalculator.calculateDisposableIncome(
                caseData)).willReturn(-100.989999);
            given(claimantResponseUtils.getClaimantRepaymentType(caseData)).willReturn("Immediately");
            given(claimantResponseUtils.getDefendantRepaymentOption(caseData)).willReturn("By a set date");
            given(claimantResponseUtils.getClaimantFinalRepaymentDate(caseData)).willReturn(null);
            given(claimantResponseUtils.getDefendantFinalRepaymentDate(caseData)).willReturn(LocalDate.of(
                2024,
                10,
                10
            ));
            InterlocutoryJudgementDoc interlocutoryJudgementDoc = getInterlocutoryJudgementDoc();
            // When
            InterlocutoryJudgementDoc actual = mapper.toInterlocutoryJudgementDoc(caseData);

            // Then
            MatcherAssert.assertThat(actual, samePropertyValuesAs(interlocutoryJudgementDoc));
        }
    }

    @Test
    void shouldMapClaimantResponseToDefendantAdmissionWhenResponseTypeSpecIsFull() {

        //Given
        caseData = getCaseData();
        caseData = caseData.toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        given(repaymentPlanDecisionCalculator.calculateDisposableIncome(
            caseData)).willReturn(-100.989999);

        // When
        InterlocutoryJudgementDoc actual = mapper.toInterlocutoryJudgementDoc(caseData);

        // Then
        assertEquals(actual.getClaimantResponseToDefendantAdmission(), "I accept full admission");
    }

    @Test
    void shouldMapFormattedDisposableIncomeWhenDisposableIncomeIsPositive() {

        //Given
        caseData = getCaseData();
        caseData = caseData.toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        given(repaymentPlanDecisionCalculator.calculateDisposableIncome(
            caseData)).willReturn(100.989999);

        // When
        InterlocutoryJudgementDoc actual = mapper.toInterlocutoryJudgementDoc(caseData);

        // Then
        assertEquals(actual.getFormattedDisposableIncome(), "£100.99");
    }

    @Test
    void shouldMapDefendantRepaymentLastDateByWhenBySetDate() {

        //Given
        caseData = getCaseData();
        caseData = caseData.toBuilder()
            .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.of(2024, 10, 10))
            )
            .build();

        // When
        InterlocutoryJudgementDoc actual = mapper.toInterlocutoryJudgementDoc(caseData);

        // Then
        assertEquals(LocalDate.of(2024, 10, 10), actual.getCourtDecisionRepaymentLastDateBy());
    }

    @Test
    void shouldMapClaimantRequestRepaymentLastDateByWhenBySetDate() {

        //Given
        caseData = getCaseData();
        caseData = caseData.toBuilder()
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.SET_DATE)
            .applicant1RequestedPaymentDateForDefendantSpec(new PaymentBySetDate()
                                                                .setPaymentSetDate(LocalDate.of(2024, 1, 1))
                                                                )
            .build();

        // When
        InterlocutoryJudgementDoc actual = mapper.toInterlocutoryJudgementDoc(caseData);

        // Then
        assertEquals(LocalDate.of(2024, 1, 1), actual.getClaimantRequestRepaymentLastDateBy());
    }

    @Test
    void shouldMapClaimantRequestRepaymentLastDateByWhenImmediately() {

        //Given
        caseData = getCaseData();
        caseData = caseData.toBuilder()
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .build();
        given(calculatorService.calculateExtendedDeadline(
            any(LocalDate.class),
            anyInt()
        )).willReturn(LocalDate.now().plusDays(5));

        // When
        InterlocutoryJudgementDoc actual = mapper.toInterlocutoryJudgementDoc(caseData);

        // Then
        assertEquals(LocalDate.now().plusDays(5), actual.getClaimantRequestRepaymentLastDateBy());
    }

    @Test
    void shouldMapRejectionReason() {

        //Given
        caseData = getCaseData();
        caseData = caseData.toBuilder()
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                        .setApplicant1RejectedRepaymentReason("Rejected in test")))
            .build();

        // When
        InterlocutoryJudgementDoc actual = mapper.toInterlocutoryJudgementDoc(caseData);

        // Then
        assertEquals("Rejected in test", actual.getRejectionReason());
    }

    private static InterlocutoryJudgementDoc getInterlocutoryJudgementDoc() {
        return new InterlocutoryJudgementDoc()
            .setClaimIssueDate(ISSUE_DATE)
            .setClaimNumber(CLAIM_NUMBER)
            .setClaimantRequestRepaymentBy("Immediately")
            .setClaimantResponseSubmitDateTime(CLAIMANT_RESPONSE_DATE_TIME)
            .setClaimantResponseToDefendantAdmission("I accept part admission")
            .setCourtDecisionRepaymentBy("By a set date")
            .setCourtDecisionRepaymentLastDateBy(LocalDate.of(2024, 10, 10))
            .setFormalisePaymentBy(REFER_TO_JUDGE)
            .setFormattedDisposableIncome("-£100.98")
            .setRejectionReason("Rejected");
    }

    private static CaseData getCaseData() {
        return CaseData.builder()
            .issueDate(ISSUE_DATE)
            .legacyCaseReference(CLAIM_NUMBER)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1ResponseDate(CLAIMANT_RESPONSE_DATE_TIME)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                        .setApplicant1RejectedRepaymentReason("Rejected")))
            .build();
    }

}
