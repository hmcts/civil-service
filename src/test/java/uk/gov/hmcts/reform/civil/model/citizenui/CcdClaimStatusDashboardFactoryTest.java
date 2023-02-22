package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentUponCourtOrder;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
class CcdClaimStatusDashboardFactoryTest {

    @InjectMocks
    private DashboardClaimStatusFactory ccdClaimStatusDashboardFactory;

    @Test
    void given_hasResponsePending_whenGetStatus_thenReturnNoResponse() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDateTime.of(2022, 2, 2, 16, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    void given_hasResponseDueToday_whenGetStatus_thenReturnResponseDueNow() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().atTime(10, 0, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_DUE_NOW);
    }

    @Test
    void given_moreTimeRequested_whenGetStatus_thenReturnMoreTimeRequested() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1TimeExtensionDate(LocalDateTime.now().plusDays(30))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_TIME_REQUESTED);
    }

    @Test
    void given_responseAdmitPayImmediately_whenGetStatus_thenReturnAdmitPayImmediately() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY);
    }

    @Test
    void given_responseAdmitPayBySetDate_whenGetStatus_thenReturnAdmitPayBySetDate() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE);
    }

    @Test
    void given_responseAdmitPayByInstallments_whenGetStatus_thenReturnAdmitPayByInstallments() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_INSTALLMENTS);
    }

    @Test
    void given_claimantConfirmedDefendantPaid_whenGetStatus_thenReturnClaimantAcceptedStatesPaid() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1CourtOrderPayment(new PaymentUponCourtOrder(YesOrNo.NO, Collections.emptyList()))
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    void given_defendantPayedInFull_whenGetStatus_thenReturnSettled() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimantAcceptedDefendantResponse_whenGetStatus_thenReturnSettled() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimIsSentToCourt_whenGetStatus_thenReturnTransferred() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.TRANSFERRED);
    }

    @Test
    void given_claimantRequestedCountyCourtJudgement_whenGetStatus_thenReturnRequestedCountryCourtJudgement() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .applicant1DQ(Applicant1DQ.builder().applicant1DQRequestedCourt(RequestedCourt.builder().build()).build())
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT);
    }

    @Test
    void given_claimantAcceptedAdmission_whenGetStatus_thenReturnRelevantStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT);
    }

    @Test
    void given_defendantRespondedWithPartAdmit_whenGetStatus_thenReturnRelevantStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_PART_ADMIT);
    }

    private static CaseData getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec paymentMethod) {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(paymentMethod)
            .build();
        return claim;
    }

}
