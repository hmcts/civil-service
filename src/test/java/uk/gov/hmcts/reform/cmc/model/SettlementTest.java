package uk.gov.hmcts.reform.cmc.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.MadeBy;
import uk.gov.hmcts.reform.hmc.model.Offer;
import uk.gov.hmcts.reform.hmc.model.PartyStatement;
import uk.gov.hmcts.reform.hmc.model.PaymentIntention;
import uk.gov.hmcts.reform.hmc.model.Settlement;
import uk.gov.hmcts.reform.hmc.model.StatementType;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SettlementTest {

    @Test
    void givenSettelmentRejectedByDefendant_whenIsAcceptedByClaimant_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(
                PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.CLAIMANT)
                .build(),
                PartyStatement
                .builder()
                .type(StatementType.REJECTION)
                .madeBy(MadeBy.DEFENDANT)
                .build()
        ));
        assertThat(settlement.isAcceptedByClaimant()).isFalse();
    }

    @Test
    void givenSettlementAcceptedByDefendantButNotByClaimant_whenIsAcceptedByClaimant_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(PartyStatement
                                                          .builder()
                                                          .type(StatementType.ACCEPTATION)
                                                          .madeBy(MadeBy.DEFENDANT)
                                                          .build()));
        assertThat(settlement.isAcceptedByClaimant()).isFalse();
    }

    @Test
    void givenNoPartyStatements_whenIsAcceptedByClaimant_thenReturnFalse() {
        Settlement settlement = getSettlement(null);
        assertThat(settlement.isAcceptedByClaimant()).isFalse();
    }

    @Test
    void givenPartyStatementHasAcceptedByClaimantAndNoRejectionsFromDefendant_whenIsAcceptedByClaimant_thenReturnTrue() {
        Settlement settlement = getSettlement(List.of(
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.CLAIMANT)
                .build(),
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.DEFENDANT)
                .build()
        ));
        assertThat(settlement.isAcceptedByClaimant()).isTrue();
    }

    @Test
    void givenPartyStatementHasCounterSign_whenIsSettled_thenReturnTrue() {
        Settlement settlement = getSettlement(List.of(PartyStatement
                                                          .builder()
                                                          .type(StatementType.COUNTERSIGNATURE)
                                                          .build()));
        assertThat(settlement.isSettled()).isTrue();
    }

    @Test
    void givenPartyStatementDoesNotHaveCounterSign_whenIsSettled_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.CLAIMANT)
                .build(),
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.DEFENDANT)
                .build()
        ));
        assertThat(settlement.isSettled()).isFalse();
    }

    @Test
    void givenPartyStatementIsOfferAndNoPaymentIntention_whenIsThroughAdmissions_thenReturnTrue() {
        Settlement settlement = getSettlement(List.of(
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.CLAIMANT)
                .build(),
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.DEFENDANT)
                .build(),
            PartyStatement
                .builder()
                .type(StatementType.OFFER)
                .offer(Offer.builder().build())
                .build()
        ));
        assertThat(settlement.isThroughAdmissions()).isTrue();
    }

    @Test
    void givenPartyStatementIsOfferAndHasPaymentIntention_whenIsThroughAdmissions_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.CLAIMANT)
                .build(),
            PartyStatement
                .builder()
                .type(StatementType.ACCEPTATION)
                .madeBy(MadeBy.DEFENDANT)
                .build(),
            PartyStatement
                .builder()
                .type(StatementType.OFFER)
                .offer(Offer.builder().build())
                .build(),
            PartyStatement
                .builder()
                .type(StatementType.OFFER)
                .offer(Offer.builder()
                           .paymentIntention(PaymentIntention
                                                 .builder()
                                                 .build())
                           .build())
                .build()
        ));
        assertThat(settlement.isThroughAdmissions()).isFalse();
    }

    private Settlement getSettlement(List<PartyStatement> partyStatements) {
        return Settlement.builder()
            .partyStatements(partyStatements).build();
    }
}
