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
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.of(2022, 2, 2));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    void given_hasResponseDueToday_whenGetStatus_thenReturnResponseDueNow() {
        LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
        try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mock.when(LocalDateTime::now).thenReturn(now);
            CmcClaim claim = new CmcClaim()
                .setResponseDeadline(now.toLocalDate());
            DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
            assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_DUE_NOW);
        }
    }

    @Test
    void given_moreTimeRequested_whenGetStatus_thenReturnMoreTimeRequested() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setMoreTimeRequested(true);
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
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECTED_PAYMENT_PLAN);
    }

    @Test
    void given_responseAdmitPayByInstallments_whenGetStatus_thenReturnAdmitPayByInstallments() {
        CmcClaim claim = getFullAdmitClaim(PaymentOption.INSTALMENTS);
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECTED_PAYMENT_PLAN);
    }

    @Test
    void given_claimantConfirmedDefendantPaid_whenGetStatus_thenReturnClaimantAcceptedStatesPaid() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setMoneyReceivedOn(LocalDate.now())
            .setCountyCourtJudgmentRequestedAt(LocalDateTime.now());
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    void given_defendantPayedInFull_whenGetStatus_thenReturnSettled() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setMoneyReceivedOn(LocalDate.now());

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimantAcceptedDefendantResponse_whenGetStatus_thenReturnSettled() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setClaimantResponse(new ClaimantResponse().setType(ClaimantResponseType.ACCEPTATION));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimIsSentToCourt_whenGetStatus_thenReturnTransferred() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setState(ClaimState.TRANSFERRED);

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.TRANSFERRED);
    }

    @Test
    void given_claimantRequestedCountyCourtJudgement_whenGetStatus_thenReturnRequestedCountryCourtJudgement() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setClaimantResponse(new ClaimantResponse())
            .setCountyCourtJudgmentRequestedAt(LocalDateTime.now());
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT);
    }

    @Test
    void given_claimantHasRequestedRedetermination_whenGetStatus_thenReturnRequestCCJByRedetermination() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setClaimantResponse(new ClaimantResponse())
            .setReDeterminationRequestedAt(LocalDateTime.now());
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_CCJ_BY_REDETERMINATION);
    }

    @Test
    void given_hasCourtDetermination_whenGetStatus_thenReturnRequestCCJByRedetermination() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setClaimantResponse(new ClaimantResponse().setCourtDetermination(new CourtDetermination()));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_CCJ_BY_REDETERMINATION);
    }

    @Test
    void given_settlementBreachedAndCCJRaised_whenGetStatus_thenReturnRequestCCJ() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setClaimantResponse(new ClaimantResponse())
            .setCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .setSettlement(new Settlement()
                            .setPartyStatements(List.of(new PartyStatement()
                                                         .setType(StatementType.OFFER))));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT);
    }

    @Test
    void given_defendantRejectedTheClaimAndNoClaimantResponse_whenGetStatus_thenReturnWaitingForClaimantResponse() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response().setResponseType(RespondentResponseType.FULL_DEFENCE));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_FOR_CLAIMANT_TO_RESPOND);
    }

    @Test
    void given_defendantHasChosenToRespondByPost_whenGetStatus_thenReturnResponseByPost() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response().setResponseMethod(ResponseMethod.OFFLINE));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_BY_POST);
    }

    @Test
    void given_defendantHasAppliedToChangeTheClaim_whenGetStatus_thenReturnChangeByDefendant() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setProceedOfflineReason(ProceedOfflineReasonType.APPLICATION_BY_DEFENDANT);
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CHANGE_BY_DEFENDANT);
    }

    @Test
    void given_defendantHasAppliedToChangeTheClaim_whenGetStatus_thenReturnChangeByClaimant() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setProceedOfflineReason(ProceedOfflineReasonType.APPLICATION_BY_CLAIMANT);
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CHANGE_BY_CLAIMANT);
    }

    @Test
    void given_claimPassedToCountryCourtBusinessCentre_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setState(ClaimState.BUSINESS_QUEUE);
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.PASSED_TO_COUNTRY_COURT_BUSINESS_CENTRE);
    }

    @Test
    void given_claimantAcceptedAdmission_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response()
                          .setResponseType(RespondentResponseType.PART_ADMISSION)
                          .setPaymentIntention(new PaymentIntention()
                                                .setPaymentOption(PaymentOption.IMMEDIATELY)
                                                .setPaymentDate(LocalDate.now().minusDays(2))))
            .setClaimantResponse(new ClaimantResponse().setType(ClaimantResponseType.ACCEPTATION));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT);
    }

    @Test
    void given_defendantRespondedWithPartAdmit_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response()
                          .setResponseType(RespondentResponseType.PART_ADMISSION)
                          .setPaymentIntention(new PaymentIntention().setPaymentOption(PaymentOption.IMMEDIATELY)));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_PART_ADMIT);
    }

    @Test
    void given_claimantSubmittedSettlement_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setClaimantResponse(new ClaimantResponse())
            .setSettlement(new Settlement()
                            .setPartyStatements(List.of(new PartyStatement()
                                                         .setMadeBy(MadeBy.CLAIMANT)
                                                         .setType(StatementType.ACCEPTATION))));
        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ASKED_FOR_SETTLEMENT);
    }

    @Test
    void given_bothPartiesSignedSettlementAgreement_whenGetStatus_thenReturnRelevantStatus() {
        CmcClaim claim = new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response())
            .setClaimantResponse(new ClaimantResponse())
            .setSettlement(new Settlement()
                            .setPartyStatements(List.of(new PartyStatement()
                                                         .setMadeBy(MadeBy.CLAIMANT)
                                                         .setType(StatementType.ACCEPTATION),
                                                     new PartyStatement()
                                                         .setMadeBy(MadeBy.DEFENDANT)
                                                         .setType(StatementType.ACCEPTATION),
                                                     new PartyStatement()
                                                         .setType(StatementType.COUNTERSIGNATURE))));

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLEMENT_SIGNED);
    }

    @Test
    void given_respondentFullDefenceAndClaimantReject_whenGetStatus_thenReturnClaimEnded() {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response().setResponseType(RespondentResponseType.FULL_DEFENCE))
            .setClaimantResponse(new ClaimantResponse().setType(ClaimantResponseType.REJECTION));

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_ENDED);
    }

    @Test
    void given_respondentFullDefenceAndSentToCourtAndClaimantAccept_whenGetStatus_thenReturnClaimantRejectOfferOutOfCourt() {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response().setResponseType(RespondentResponseType.FULL_DEFENCE))
            .setSettlement(new Settlement()
                            .setPartyStatements(List.of(new PartyStatement()
                                                         .setType(StatementType.REJECTION))));

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECT_OFFER_OUT_OF_COURT);
    }

    @Test
    void given_respondentFullDefenceAndSentToCourtAndClaimantAccept_whenGetStatus_thenReturnClaimantAcceptedOfferOutOfCourt() {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response().setResponseType(RespondentResponseType.FULL_DEFENCE))
            .setSettlement(new Settlement()
                            .setPartyStatements(List.of(new PartyStatement()
                                                         .setMadeBy(MadeBy.CLAIMANT)
                                                         .setType(StatementType.ACCEPTATION))));

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_OFFER_OUT_OF_COURT);
    }

    @Test
    void given_respondentFullDefenceAndSentToCourtAndClaimantAccept_whenGetStatus_thenReturnPartialRejected() {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response().setResponseType(RespondentResponseType.PART_ADMISSION))
            .setClaimantResponse(new ClaimantResponse().setType(ClaimantResponseType.REJECTION));

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECT_PARTIAL_ADMISSION);
    }

    @Test
    void given_respondentFullDefence_whenGetStatus_claimRejectedOfferSettleOutOfCourt() {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response()
                          .setResponseType(RespondentResponseType.FULL_DEFENCE))
            .setSettlement(new Settlement());

        DashboardClaimStatus status = cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_REJECTED_OFFER_SETTLE_OUT_OF_COURT);
    }

    private CmcClaim getFullAdmitClaim(PaymentOption paymentOption) {
        return new CmcClaim()
            .setResponseDeadline(LocalDate.now().plusDays(10))
            .setResponse(new Response()
                          .setResponseType(RespondentResponseType.FULL_ADMISSION)
                          .setPaymentIntention(new PaymentIntention()
                                                .setPaymentOption(paymentOption)));
    }
}
