package uk.gov.hmcts.reform.cmc.model;

import java.time.LocalDateTime;
import org.assertj.core.api.AssertionsForClassTypes;
import org.elasticsearch.core.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatus;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class CmcClaimTest {

    private static final String NAME = "Mr John Clark";
    @InjectMocks
    private DashboardClaimStatusFactory cmcClaimStatusDashboardFactory;

    @Test
    void shouldReturnClaimantNameWhenClaimantExists() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().claimants(Arrays.asList(CmcParty.builder()
                                                                       .name(NAME)
                                                                       .build()))
                           .build())
            .build();
        String claimantName = cmcClaim.getClaimantName();
        assert (claimantName).equals(NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenClaimantDoesNotExist() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().claimants(Collections.emptyList()).build())
            .build();
        String claimantName = cmcClaim.getClaimantName();
        assert (claimantName).equals("");
    }

    @Test
    void shouldReturnDefendantNameWhenDefendantExists() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().defendants(Arrays.asList(CmcParty.builder().name(NAME)
                                                                        .build()))
                           .build())
            .build();
        String defendantName = cmcClaim.getDefendantName();
        assert (defendantName).equals(NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenDefendantDoesNotExist() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().defendants(Collections.emptyList()).build())
            .build();
        String defendantName = cmcClaim.getDefendantName();
        assert (defendantName).equals("");
    }

    @Test
    void shouldReturnPayedByDateWhenItExists() {
        //Given
        LocalDate now = LocalDate.now();
        CmcClaim cmcClaim = CmcClaim.builder()
            .response(Response.builder().paymentIntention(PaymentIntention.builder()
                                                              .paymentDate(now).build())
                          .build()).build();
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isEqualTo(now);
    }

    @Test
    void shouldReturnNullWhenNoResponseExists() {
        //Given
        CmcClaim cmcClaim = CmcClaim.builder().build();
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isNull();
    }

    @Test
    void shouldReturnNullWhenNoPaymentIntentionExists() {
        //Given
        CmcClaim cmcClaim = CmcClaim.builder()
            .response(Response.builder().build()).build();
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isNull();
    }

    @Test
    void shouldReturnTrueWhenClaimRejectedAndOfferSettleOutOfCourt() {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder()
                          .responseType(RespondentResponseType.FULL_DEFENCE)
                          .build())
            .settlement(Settlement.builder()
                            .partyStatements(List.of(PartyStatement.builder().build()))
                            .build())
            .build();

        assertTrue(claim.isClaimRejectedAndOfferSettleOutOfCourt());
    }

    @Test
    void shouldReturnTrueWhenClaimantRejectOffer()  {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder()
                          .responseType(RespondentResponseType.FULL_DEFENCE)
                          .build())
            .settlement(Settlement.builder()
                            .partyStatements(List.of(PartyStatement.builder()
                                                         .type(StatementType.REJECTION)
                                                         .build()))
                            .build())
            .build();

        assertTrue(claim.hasClaimantRejectOffer());
    }

    @Test
    void given_claimantNotRespondedWithInDeadLine_whenGetStatus_claimEnded() {
        CmcClaim claim = CmcClaim.builder()
            .response(Response.builder()
                          .responseType(RespondentResponseType.FULL_DEFENCE)
                          .build())
            .intentionToProceedDeadline(LocalDate.now().minusDays(1))
            .build();
        DashboardClaimStatus status =
            cmcClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        AssertionsForClassTypes.assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_ENDED);
    }

    @Test
    void shouldReturnTrueIfOfferAcceptedAndClaimantSignedSettlementAgreement() {
        //Given
        CmcClaim claim = CmcClaim.builder()
                .response(Response.builder()
                        .responseType(RespondentResponseType.FULL_ADMISSION)
                        .build())
                .settlement(Settlement.builder()
                        .partyStatements(List.of(
                                PartyStatement.builder()
                                        .type(StatementType.OFFER)
                                        .offer(Offer.builder()
                                               .paymentIntention(PaymentIntention.builder().build())
                                               .build())
                                        .build(),
                                PartyStatement.builder()
                                        .type(StatementType.ACCEPTATION)
                                        .build()))
                        .build())
                .claimantResponse(ClaimantResponse.builder().build())
                .build();
        //When
        boolean signed = claim.hasClaimantSignedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfClaimantSignedSettlementAgreementChosenByCourt() {
        //Given
        CmcClaim claim = CmcClaim.builder()
                .response(Response.builder()
                        .responseType(RespondentResponseType.FULL_ADMISSION)
                        .build())
                .settlement(Settlement.builder()
                        .partyStatements(List.of(
                                PartyStatement.builder()
                                        .type(StatementType.OFFER)
                                        .offer(Offer.builder()
                                               .paymentIntention(PaymentIntention.builder().build())
                                               .build())
                                        .build(),
                                PartyStatement.builder()
                                        .type(StatementType.ACCEPTATION)
                                        .build()))
                        .build())
                .claimantResponse(ClaimantResponse.builder()
                        .courtDetermination(CourtDetermination.builder().build())
                        .build())
                .build();
        //When
        boolean signed = claim.hasClaimantSignedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfSettlementAgreementDeadlineExpired() {
        //Given
        CmcClaim claim = CmcClaim.builder()
                .response(Response.builder()
                        .responseType(RespondentResponseType.FULL_ADMISSION)
                        .build())
                .settlement(Settlement.builder()
                        .partyStatements(List.of(
                                PartyStatement.builder()
                                        .type(StatementType.OFFER)
                                        .offer(Offer.builder()
                                               .paymentIntention(PaymentIntention.builder().build())
                                               .build())
                                        .build(),
                                PartyStatement.builder()
                                        .type(StatementType.ACCEPTATION)
                                        .build()))
                        .build())
                .claimantRespondedAt(LocalDateTime.MIN)
                .build();
        //When
        boolean signed = claim.hasClaimantSignedSettlementAgreementAndDeadlineExpired();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfBothSignedSettlementAgreement() {
        //Given
        CmcClaim claim = CmcClaim.builder()
                .response(Response.builder()
                        .responseType(RespondentResponseType.FULL_ADMISSION)
                        .build())
                .settlement(Settlement.builder()
                        .partyStatements(List.of(
                                PartyStatement.builder()
                                        .type(StatementType.OFFER)
                                        .offer(Offer.builder()
                                               .paymentIntention(PaymentIntention.builder().build())
                                               .build())
                                        .build(),
                                PartyStatement.builder()
                                        .type(StatementType.ACCEPTATION)
                                        .build(),
                                PartyStatement.builder()
                                        .type(StatementType.COUNTERSIGNATURE)
                                        .build()))
                        .build())
                .build();
        //When
        boolean signed = claim.hasClaimantAndDefendantSignedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }

    @Test
    void shouldReturnTrueIfDefendantRejectedSettlementAgreement() {
        //Given
        CmcClaim claim = CmcClaim.builder()
                .response(Response.builder()
                        .responseType(RespondentResponseType.FULL_ADMISSION)
                        .build())
                .settlement(Settlement.builder()
                        .partyStatements(List.of(
                                PartyStatement.builder()
                                        .type(StatementType.OFFER)
                                        .offer(Offer.builder()
                                               .paymentIntention(PaymentIntention.builder().build())
                                               .build())
                                        .build(),
                                PartyStatement.builder()
                                        .type(StatementType.REJECTION)
                                        .build()))
                        .build())
                .claimantResponse(ClaimantResponse.builder()
                        .type(ClaimantResponseType.ACCEPTATION)
                        .formaliseOption(FormaliseOption.SETTLEMENT)
                        .build())
                .build();
        //When
        boolean signed = claim.hasDefendantRejectedSettlementAgreement();
        //Then
        assertThat(signed).isTrue();
    }
}
