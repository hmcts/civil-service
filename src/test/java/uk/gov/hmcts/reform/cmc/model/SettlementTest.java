package uk.gov.hmcts.reform.cmc.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SettlementTest {

    @Test
    void givenSettelmentRejectedByDefendant_whenIsAcceptedByClaimant_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.CLAIMANT),
            new PartyStatement()
                .setType(StatementType.REJECTION)
                .setMadeBy(MadeBy.DEFENDANT)
        ));
        assertThat(settlement.isAcceptedByClaimant()).isFalse();
    }

    @Test
    void givenSettlementAcceptedByDefendantButNotByClaimant_whenIsAcceptedByClaimant_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(new PartyStatement()
                                                          .setType(StatementType.ACCEPTATION)
                                                          .setMadeBy(MadeBy.DEFENDANT)));
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
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.CLAIMANT),
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.DEFENDANT)
        ));
        assertThat(settlement.isAcceptedByClaimant()).isTrue();
    }

    @Test
    void givenPartyStatementHasCounterSign_whenIsSettled_thenReturnTrue() {
        Settlement settlement = getSettlement(List.of(new PartyStatement()
                                                          .setType(StatementType.COUNTERSIGNATURE)));
        assertThat(settlement.isSettled()).isTrue();
    }

    @Test
    void givenPartyStatementDoesNotHaveCounterSign_whenIsSettled_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.CLAIMANT),
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.DEFENDANT)
        ));
        assertThat(settlement.isSettled()).isFalse();
    }

    @Test
    void givenPartyStatementIsOfferAndNoPaymentIntention_whenIsThroughAdmissions_thenReturnTrue() {
        Settlement settlement = getSettlement(List.of(
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.CLAIMANT),
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.DEFENDANT),
            new PartyStatement()
                .setType(StatementType.OFFER)
                .setOffer(new Offer())
        ));
        assertThat(settlement.isThroughAdmissions()).isTrue();
    }

    @Test
    void givenPartyStatementIsOfferAndHasPaymentIntention_whenIsThroughAdmissions_thenReturnFalse() {
        Settlement settlement = getSettlement(List.of(
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.CLAIMANT),
            new PartyStatement()
                .setType(StatementType.ACCEPTATION)
                .setMadeBy(MadeBy.DEFENDANT),
            new PartyStatement()
                .setType(StatementType.OFFER)
                .setOffer(new Offer()),
            new PartyStatement()
                .setType(StatementType.OFFER)
                .setOffer(new Offer()
                           .setPaymentIntention(new PaymentIntention()))
        ));
        assertThat(settlement.isThroughAdmissions()).isFalse();
    }

    private Settlement getSettlement(List<PartyStatement> partyStatements) {
        return new Settlement().setPartyStatements(partyStatements);
    }
}
