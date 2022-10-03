package uk.gov.hmcts.reform.fees.client;

import java.math.BigDecimal;

enum ClaimIssueFeeKeyword {
    HearingFeeUpTo300,
    HearingFeeUpTo500,
    HearingFeeUpTo1000,
    HearingFeeUpTo1500,
    HearingFeeUpTo3k,
    HearingFeeAbove3k,
    PaperClaimUpTo10k,
    PaperClaimUpTo200k,
    PaperClaimAbove200k,
    UnspecifiedClaim;

    public static String getKeywordIssueEvent(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.valueOf(0.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(300)) <= 0) {
            return HearingFeeUpTo300.name();
        } else if (amount.compareTo(BigDecimal.valueOf(300.01)) >= 0
                  && amount.compareTo(BigDecimal.valueOf(500)) <= 0) {
            return HearingFeeUpTo500.name();
        } else if (amount.compareTo(BigDecimal.valueOf(500.01)) >= 0
                  && amount.compareTo(BigDecimal.valueOf(1000)) <= 0) {
            return HearingFeeUpTo1000.name();
        } else if (amount.compareTo(BigDecimal.valueOf(1000.01)) >= 0
                  && amount.compareTo(BigDecimal.valueOf(1500)) <= 0) {
            return HearingFeeUpTo1500.name();
        } else if (amount.compareTo(BigDecimal.valueOf(1500.01)) >= 0
                  && amount.compareTo(BigDecimal.valueOf(3000)) <= 0) {
            return HearingFeeUpTo3k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(3000.01)) >= 0
                  && amount.compareTo(BigDecimal.valueOf(5000)) <= 0) {
            return HearingFeeAbove3k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(5000.01)) >= 0
                  && amount.compareTo(BigDecimal.valueOf(10000)) <= 0) {
            return PaperClaimUpTo10k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(10000.01)) >= 0
                  && amount.compareTo(BigDecimal.valueOf(200000)) <= 0) {
            return PaperClaimUpTo200k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(200000.01)) >= 0) {
            return PaperClaimAbove200k.name();
        } else {
            return UnspecifiedClaim.name();
        }
    }
}
