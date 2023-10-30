package uk.gov.hmcts.reform.civil.model.citizenui.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepaymentDecisionTypeTest {

    @Test
    void shouldReturnTrue_whenRepaymentDecisionIsInFavourOfDefendant() {
        //Given
        RepaymentDecisionType repaymentDecisionType = RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT;
        //Then
        assertTrue(repaymentDecisionType.isInFavourOfDefendant());
    }

    @Test
    void shouldReturnFalse_whenRepaymentDecisionIsInFavourOfClaimant() {
        //Given
        RepaymentDecisionType repaymentDecisionType = RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT;
        //Then
        assertFalse(repaymentDecisionType.isInFavourOfDefendant());
    }
}
