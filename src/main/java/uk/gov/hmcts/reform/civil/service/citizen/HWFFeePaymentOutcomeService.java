package uk.gov.hmcts.reform.civil.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public final class HWFFeePaymentOutcomeService {

    public CaseData updateOutstandingFee(CaseData caseData) {

        BigDecimal claimIssuedRemissionAmount = caseData.getClaimIssueRemissionAmount();
        BigDecimal hearingRemissionAmount = caseData.getHearingRemissionAmount();
        BigDecimal claimFeeAmount = caseData.getCalculatedClaimFeeInPence();
        BigDecimal hearingFeeAmount = caseData.getCalculatedHearingFeeInPence();
        BigDecimal outstandingFeeAmount;

        if (caseData.isHWFTypeClaimIssued() && BigDecimal.ZERO.compareTo(claimFeeAmount) != 0) {
            outstandingFeeAmount = claimFeeAmount.subtract(claimIssuedRemissionAmount);
          caseData.getClaimIssuedHwfDetails().setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(
                outstandingFeeAmount));
        } else if (caseData.isHWFTypeHearing() && BigDecimal.ZERO.compareTo(hearingFeeAmount) != 0) {
            outstandingFeeAmount = hearingFeeAmount.subtract(hearingRemissionAmount);
            caseData.getHearingHwfDetails().setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(
                outstandingFeeAmount));
        }
        return caseData;
    }
}
