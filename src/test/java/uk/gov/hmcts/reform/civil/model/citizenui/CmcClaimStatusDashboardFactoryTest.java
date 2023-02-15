package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.cmc.model.ClaimState;
import uk.gov.hmcts.reform.cmc.model.ClaimantResponse;
import uk.gov.hmcts.reform.cmc.model.ClaimantResponseType;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;
import uk.gov.hmcts.reform.cmc.model.PaymentIntention;
import uk.gov.hmcts.reform.cmc.model.PaymentOption;
import uk.gov.hmcts.reform.cmc.model.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(SpringExtension.class)
public class CmcClaimStatusDashboardFactoryTest {

    @InjectMocks
    private DashboardClaimStatusFactory ccdClaimStatusDashboardFactory;

    @Test
    void given_hasResponsePending_whenGetStatus_thenReturnNoResponse() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10)).build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.of(2022, 2, 2))
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    void given_hasResponseDueToday_whenGetStatus_thenReturnResponseDueNow() {
        LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
        try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mock.when(LocalDateTime::now).thenReturn(now);
            CmcClaim claim = CmcClaim.builder()
                .responseDeadline(now.toLocalDate())
                .build();
            DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
            assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_DUE_NOW);
        }
    }

    @Test
    void given_moreTimeRequested_whenGetStatus_thenReturnMoreTimeRequested() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .moreTimeRequested(true).build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_TIME_REQUESTED);
    }

    @Test
    void given_responseAdmitPayImmediately_whenGetStatus_thenReturnAdmitPayImmediately() {
        CmcClaim claim = getFullAdmitClaim(PaymentOption.IMMEDIATELY);
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY);
    }

    @Test
    void given_responseAdmitPayBySetDate_whenGetStatus_thenReturnAdmitPayBySetDate() {
        CmcClaim claim = getFullAdmitClaim(PaymentOption.BY_SPECIFIED_DATE);
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE);
    }

    @Test
    void given_responseAdmitPayByInstallments_whenGetStatus_thenReturnAdmitPayByInstallments() {
        CmcClaim claim = getFullAdmitClaim(PaymentOption.INSTALMENTS);
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_INSTALLMENTS);
    }

    @Test
    void given_claimantConfirmedDefendantPaid_whenGetStatus_thenReturnClaimantAcceptedStatesPaid() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .moneyReceivedOn(LocalDate.now())
            .countyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    void given_defendantPayedInFull_whenGetStatus_thenReturnSettled() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .moneyReceivedOn(LocalDate.now())
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimantAcceptedDefendantResponse_whenGetStatus_thenReturnSettled() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().type(ClaimantResponseType.ACCEPTATION).build())
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimIsSentToCourt_whenGetStatus_thenReturnTransferred() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .state(ClaimState.TRANSFERRED).build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.TRANSFERRED);
    }

    @Test
    void given_claimantRequestedCountyCourtJudgement_whenGetStatus_thenReturnRequestedCountryCourtJudgement() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().build())
            .countyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT);
    }

    private CmcClaim getFullAdmitClaim(PaymentOption paymentOption) {
        return CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder()
                          .responseType(RespondentResponseType.FULL_ADMISSION)
                          .paymentIntention(PaymentIntention.builder()
                                                .paymentOption(paymentOption)
                                                .build())
                          .build())
            .build();
    }

}



