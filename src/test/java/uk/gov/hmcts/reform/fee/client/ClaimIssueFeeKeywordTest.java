package uk.gov.hmcts.reform.fee.client;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fees.client.ClaimIssueFeeKeyword;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClaimIssueFeeKeywordTest {

    @Nested
    class GetKeywordIssueEvent {
        @Test
        void shouldReturnUnspecifiedClaim_UnSpecifiedAndAmountIsNotZero() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(10.00), false);
            assertEquals(keyword, "UnspecifiedClaim");
        }

        @Test
        void shouldReturnUnspecifiedClaim_UnSpecifiedAndAmountIsZero() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(0.00), false);
            assertEquals(keyword, "UnspecifiedClaim");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified0() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(0.00), true);
            assertEquals(keyword, "UnspecifiedClaim");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified0To300() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(250.00), true);
            assertEquals(keyword, "HearingFeeUpTo300");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified300To500() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(410.00), true);
            assertEquals(keyword, "HearingFeeUpTo500");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified500To1000() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(810.00), true);
            assertEquals(keyword, "HearingFeeUpTo1000");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified1000To1500() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(1200.00), true);
            assertEquals(keyword, "HearingFeeUpTo1500");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified1500To3000() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(2200.00), true);
            assertEquals(keyword, "HearingFeeUpTo3k");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified3000To5000() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(4600.00), true);
            assertEquals(keyword, "HearingFeeAbove3k");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified5000To10000() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(8800.00), true);
            assertEquals(keyword, "PaperClaimUpTo10k");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecified10000To200000() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(100000.00), true);
            assertEquals(keyword, "PaperClaimUpTo200k");
        }

        @Test
        void shouldReturnUnspecifiedClaim_isSpecifiedAbove200000() {
            String keyword = ClaimIssueFeeKeyword.getKeywordIssueEvent(new BigDecimal(210000.00), true);
            assertEquals(keyword, "PaperClaimAbove200k");
        }

    }
}
