package uk.gov.hmcts.reform.cmc.model;

import org.assertj.core.api.AssertionsForClassTypes;
import org.elasticsearch.core.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatus;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CmcClaimTest {

    private static final String NAME = "Mr John Clark";
    @InjectMocks
    private DashboardClaimStatusFactory cmcClaimStatusDashboardFactory;

    @Test
    void shouldReturnClaimantNameWhenClaimantExists() {
        CmcClaim cmcClaim = new CmcClaim()
            .setClaimData(new ClaimData().setClaimants(Arrays.asList(new CmcParty().setName(NAME))));
        String claimantName = cmcClaim.getClaimantName();
        assert (claimantName).equals(NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenClaimantDoesNotExist() {
        CmcClaim cmcClaim = new CmcClaim().setClaimData(new ClaimData().setClaimants(Collections.emptyList()));
        String claimantName = cmcClaim.getClaimantName();
        assert (claimantName).equals("");
    }

    @Test
    void shouldReturnDefendantNameWhenDefendantExists() {
        CmcClaim cmcClaim = new CmcClaim()
            .setClaimData(new ClaimData().setDefendants(Arrays.asList(new CmcParty().setName(NAME))));
        String defendantName = cmcClaim.getDefendantName();
        assert (defendantName).equals(NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenDefendantDoesNotExist() {
        CmcClaim cmcClaim = new CmcClaim().setClaimData(new ClaimData().setDefendants(Collections.emptyList()));
        String defendantName = cmcClaim.getDefendantName();
        assert (defendantName).equals("");
    }

    @Test
    void shouldReturnPayedByDateWhenItExists() {
        //Given
        LocalDate now = LocalDate.now();
        CmcClaim cmcClaim = new CmcClaim().setResponse(new Response().setPaymentIntention(new PaymentIntention().setPaymentDate(now)));
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isEqualTo(now);
    }

    @Test
    void shouldReturnNullWhenNoResponseExists() {
        //Given
        CmcClaim cmcClaim = new CmcClaim();
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isNull();
    }

    @Test
    void shouldReturnNullWhenNoPaymentIntentionExists() {
        //Given
        CmcClaim cmcClaim = new CmcClaim()
            .setResponse(new Response());
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isNull();
    }

    @Test
    void shouldReturnTrueWhenClaimRejectedAndOfferSettleOutOfCourt() {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response()
                          .setResponseType(RespondentResponseType.FULL_DEFENCE))
            .setSettlement(new Settlement()
                            .setPartyStatements(List.of(new PartyStatement())));

        assertTrue(claim.isClaimRejectedAndOfferSettleOutOfCourt());
    }

    @Test
    void shouldReturnTrueWhenClaimantRejectOffer()  {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response().setResponseType(RespondentResponseType.FULL_DEFENCE))
            .setSettlement(new Settlement()
                            .setPartyStatements(List.of(new PartyStatement()
                                                         .setType(StatementType.REJECTION))));

        assertTrue(claim.hasClaimantRejectOffer());
        assertNull(claim.getResponse().getPaymentIntention());
    }

    @Test
    void given_claimantNotRespondedWithInDeadLine_whenGetStatus_claimEnded() {
        CmcClaim claim = new CmcClaim()
            .setResponse(new Response().setResponseType(RespondentResponseType.FULL_DEFENCE))
            .setIntentionToProceedDeadline(LocalDate.now().minusDays(1));
        DashboardClaimStatus status =
            cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        AssertionsForClassTypes.assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_ENDED);
    }

    @Test
    void shouldReturnTrueIfOfferAcceptedAndClaimantSignedSettlementAgreement() {
        //Given
        CmcClaim claim = new CmcClaim()
                .setResponse(new Response().setResponseType(RespondentResponseType.FULL_ADMISSION))
                .setSettlement(new Settlement()
                        .setPartyStatements(List.of(
                                new PartyStatement()
                                        .setType(StatementType.OFFER)
                                        .setOffer(new Offer()
                                               .setPaymentIntention(new PaymentIntention())),
                                new PartyStatement()
                                        .setType(StatementType.ACCEPTATION))))
                .setClaimantResponse(new ClaimantResponse());
        //When
        boolean signed = claim.hasClaimantSignedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfClaimantSignedSettlementAgreementChosenByCourt() {
        //Given
        CmcClaim claim = new CmcClaim()
                .setResponse(new Response()
                        .setResponseType(RespondentResponseType.FULL_ADMISSION))
                .setSettlement(new Settlement()
                        .setPartyStatements(List.of(
                                new PartyStatement()
                                        .setType(StatementType.OFFER)
                                        .setOffer(new Offer()
                                               .setPaymentIntention(new PaymentIntention())),
                                new PartyStatement()
                                        .setType(StatementType.ACCEPTATION))))
                .setClaimantResponse(new ClaimantResponse()
                        .setCourtDetermination(new CourtDetermination()));
        //When
        boolean signed = claim.hasClaimantSignedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfSettlementAgreementDeadlineExpired() {
        //Given
        CmcClaim claim = new CmcClaim()
                .setResponse(new Response()
                        .setResponseType(RespondentResponseType.FULL_ADMISSION))
                .setSettlement(new Settlement()
                        .setPartyStatements(List.of(
                                new PartyStatement()
                                        .setType(StatementType.OFFER)
                                        .setOffer(new Offer()
                                               .setPaymentIntention(new PaymentIntention())),
                                new PartyStatement()
                                        .setType(StatementType.ACCEPTATION))))
                .setClaimantRespondedAt(LocalDateTime.MIN);
        //When
        boolean signed = claim.hasClaimantSignedSettlementAgreementAndDeadlineExpired();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfBothSignedSettlementAgreement() {
        //Given
        CmcClaim claim = new CmcClaim()
                .setResponse(new Response()
                        .setResponseType(RespondentResponseType.FULL_ADMISSION))
                .setSettlement(new Settlement()
                        .setPartyStatements(List.of(
                                new PartyStatement()
                                        .setType(StatementType.OFFER)
                                        .setOffer(new Offer()
                                               .setPaymentIntention(new PaymentIntention())),
                                new PartyStatement()
                                        .setType(StatementType.ACCEPTATION),
                                new PartyStatement()
                                        .setType(StatementType.COUNTERSIGNATURE))));
        //When
        boolean signed = claim.hasClaimantAndDefendantSignedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfDefendantRejectedSettlementAgreement() {
        //Given
        CmcClaim claim = new CmcClaim()
                .setResponse(new Response()
                        .setResponseType(RespondentResponseType.FULL_ADMISSION))
                .setSettlement(new Settlement()
                        .setPartyStatements(List.of(
                                new PartyStatement()
                                        .setType(StatementType.OFFER)
                                        .setOffer(new Offer()
                                               .setPaymentIntention(new PaymentIntention())),
                                new PartyStatement()
                                        .setType(StatementType.REJECTION))))
                .setClaimantResponse(new ClaimantResponse()
                        .setType(ClaimantResponseType.ACCEPTATION)
                        .setFormaliseOption(FormaliseOption.SETTLEMENT));
        //When
        boolean signed = claim.hasDefendantRejectedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }
}
