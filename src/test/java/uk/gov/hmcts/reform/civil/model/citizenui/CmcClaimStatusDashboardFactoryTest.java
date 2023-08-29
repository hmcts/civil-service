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
import uk.gov.hmcts.reform.cmc.model.CourtDetermination;
import uk.gov.hmcts.reform.cmc.model.MadeBy;
import uk.gov.hmcts.reform.cmc.model.PartyStatement;
import uk.gov.hmcts.reform.cmc.model.PaymentIntention;
import uk.gov.hmcts.reform.cmc.model.PaymentOption;
import uk.gov.hmcts.reform.cmc.model.ProceedOfflineReasonType;
import uk.gov.hmcts.reform.cmc.model.Response;
import uk.gov.hmcts.reform.cmc.model.ResponseMethod;
import uk.gov.hmcts.reform.cmc.model.Settlement;
import uk.gov.hmcts.reform.cmc.model.StatementType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(SpringExtension.class)
public class CmcClaimStatusDashboardFactoryTest {

    @InjectMocks
    private DashboardClaimStatusFactory cmcClaimStatusDashboardFactory;

    @Test
    void given_hasResponsePending_whenGetStatus_thenReturnNoResponse() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10)).build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.of(2022, 2, 2))
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
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
            DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
            assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_DUE_NOW);
        }
    }

    @Test
    void given_moreTimeRequested_whenGetStatus_thenReturnMoreTimeRequested() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .moreTimeRequested(true).build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_TIME_REQUESTED);
    }

    @Test
    void given_responseAdmitPayImmediately_whenGetStatus_thenReturnAdmitPayImmediately() {
        CmcClaim claim = getFullAdmitClaim(PaymentOption.IMMEDIATELY);
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY);
    }

    @Test
    void given_responseAdmitPayBySetDate_whenGetStatus_thenReturnAdmitPayBySetDate() {
        CmcClaim claim = getFullAdmitClaim(PaymentOption.BY_SPECIFIED_DATE);
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE);
    }

    @Test
    void given_responseAdmitPayByInstallments_whenGetStatus_thenReturnAdmitPayByInstallments() {
        CmcClaim claim = getFullAdmitClaim(PaymentOption.INSTALMENTS);
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
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
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    void given_defendantPayedInFull_whenGetStatus_thenReturnSettled() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .moneyReceivedOn(LocalDate.now())
            .build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimantAcceptedDefendantResponse_whenGetStatus_thenReturnSettled() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().type(ClaimantResponseType.ACCEPTATION).build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimIsSentToCourt_whenGetStatus_thenReturnTransferred() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .state(ClaimState.TRANSFERRED).build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
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
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT);
    }

    @Test
    void given_claimantHasRequestedRedetermination_whenGetStatus_thenReturnRequestCCJByRedetermination() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().build())
            .reDeterminationRequestedAt(LocalDateTime.now())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_CCJ_BY_REDETERMINATION);
    }

    @Test
    void given_hasCourtDetermination_whenGetStatus_thenReturnRequestCCJByRedetermination() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().courtDetermination(CourtDetermination.builder().build())
                                  .build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_CCJ_BY_REDETERMINATION);
    }

    @Test
    void given_settlementBreachedAndCCJRaised_whenGetStatus_thenReturnRequestCCJByRedetermination() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().build())
            .countyCourtJudgmentRequestedAt(LocalDateTime.now())
            .settlement(Settlement.builder()
                            .partyStatements(List.of(PartyStatement.builder()
                                                         .type(StatementType.OFFER)
                                                         .build())).build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_CCJ_BY_REDETERMINATION);
    }

    @Test
    void given_defendantRejectedTheClaimAndNoClaimantResponse_whenGetStatus_thenReturnWaitingForClaimantResponse() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().responseType(RespondentResponseType.FULL_DEFENCE)
                          .build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_FOR_CLAIMANT_TO_RESPOND);
    }

    @Test
    void given_defendantHasChosenToRespondByPost_whenGetStatus_thenReturnResponseByPost() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().responseMethod(ResponseMethod.OFFLINE).build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_BY_POST);
    }

    @Test
    void given_defendantHasAppliedToChangeTheClaim_whenGetStatus_thenReturnChangeByDefendant() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .proceedOfflineReason(ProceedOfflineReasonType.APPLICATION_BY_DEFENDANT)
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CHANGE_BY_DEFENDANT);
    }

    @Test
    void given_defendantHasAppliedToChangeTheClaim_whenGetStatus_thenReturnChangeByClaimant() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .proceedOfflineReason(ProceedOfflineReasonType.APPLICATION_BY_CLAIMANT)
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CHANGE_BY_CLAIMANT);
    }

    @Test
    void given_claimPassedToCountryCourtBusinessCentre_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .state(ClaimState.BUSINESS_QUEUE)
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.PASSED_TO_COUNTRY_COURT_BUSINESS_CENTRE);
    }

    @Test
    void given_claimantAcceptedAdmission_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder()
                          .responseType(RespondentResponseType.PART_ADMISSION)
                          .paymentIntention(PaymentIntention.builder()
                                                .paymentOption(PaymentOption.IMMEDIATELY)
                                                .paymentDate(LocalDate.now().minusDays(2))
                                                .build())
                          .build())
            .claimantResponse(ClaimantResponse.builder().type(ClaimantResponseType.ACCEPTATION).build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT);
    }

    @Test
    void given_defendantRespondedWithPartAdmit_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder()
                          .responseType(RespondentResponseType.PART_ADMISSION)
                          .paymentIntention(PaymentIntention.builder().paymentOption(PaymentOption.IMMEDIATELY).build())
                          .build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_PART_ADMIT);
    }

    @Test
    void given_claimantSubmittedSettlement_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().build())
            .settlement(Settlement.builder()
                            .partyStatements(List.of(PartyStatement.builder()
                                                         .madeBy(MadeBy.CLAIMANT)
                                                         .type(StatementType.ACCEPTATION)
                                                         .build()))
                            .build())
            .build();
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ASKED_FOR_SETTLEMENT);
    }

    @Test
    void given_bothPartiesSignedSettlementAgreement_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10))
            .response(Response.builder().build())
            .claimantResponse(ClaimantResponse.builder().build())
            .settlement(Settlement.builder()
                            .partyStatements(List.of(PartyStatement.builder()
                                                         .madeBy(MadeBy.CLAIMANT)
                                                         .type(StatementType.ACCEPTATION)
                                                         .build(), PartyStatement.builder()
                                                         .madeBy(MadeBy.DEFENDANT)
                                                         .type(StatementType.ACCEPTATION)
                                                         .build(), PartyStatement.builder()
                                                         .type(StatementType.COUNTERSIGNATURE)
                                                         .build())).build()).build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLEMENT_SIGNED);
    }

    @Test
    void given_respondentFullDefenceAndClaimantReject_whenGetStatus_thenReturnClaimEnded() {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder().responseType(RespondentResponseType.FULL_DEFENCE).build())
            .claimantResponse(ClaimantResponse.builder().type(ClaimantResponseType.REJECTION).build())
            .build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_ENDED);
    }

    @Test
    void given_respondentFullDefenceAndSentToCourtAndClaimantAccept_whenGetStatus_thenReturnClaimantRejectOfferOutOfCourt() {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder().responseType(RespondentResponseType.FULL_DEFENCE).build())
            .settlement(Settlement.builder()
                            .partyStatements(List.of(PartyStatement.builder()
                                                         .type(StatementType.REJECTION)
                                                         .build()))
                            .build())
            .build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECT_OFFER_OUT_OF_COURT);
    }

    @Test
    void given_respondentFullDefenceAndSentToCourtAndClaimantAccept_whenGetStatus_thenReturnClaimantAcceptedOfferOutOfCourt() {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder().responseType(RespondentResponseType.FULL_DEFENCE).build())
            .settlement(Settlement.builder()
                            .partyStatements(List.of(PartyStatement.builder()
                                                         .madeBy(MadeBy.CLAIMANT)
                                                         .type(StatementType.ACCEPTATION)
                                                         .build()))
                            .build())
            .build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_OFFER_OUT_OF_COURT);
    }

    @Test
    void given_respondentFullDefenceAndSentToCourtAndClaimantAccept_whenGetStatus_thenReturnPartialRejected() {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder().responseType(RespondentResponseType.PART_ADMISSION).build())
            .claimantResponse(ClaimantResponse.builder().type(ClaimantResponseType.REJECTION).build())
            .build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECT_PARTIAL_ADMISSION);
    }

    @Test
    void given_respondentFullDefence_whenGetStatus_claimRejectedOfferSettleOutOfCourt() {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder()
                          .responseType(RespondentResponseType.FULL_DEFENCE)
                          .build())
            .settlement(Settlement.builder()
                            .build())
            .build();

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_REJECTED_OFFER_SETTLE_OUT_OF_COURT);
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
