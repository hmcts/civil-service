package uk.gov.hmcts.reform.civil.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.service.citizenui.HelpWithFeesForTabService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;

@Slf4j
@Service
@RequiredArgsConstructor
public class HWFFeePaymentOutcomeService {

    private final HelpWithFeesForTabService helpWithFeesForTabService;

    public CaseData updateHwfReferenceNumber(CaseData caseData) {
        if (Objects.nonNull(caseData.getFeePaymentOutcomeDetails())
            && caseData.getFeePaymentOutcomeDetails().getHwfNumberAvailable() == YesOrNo.YES) {
            if (caseData.isHWFTypeClaimIssued()) {
                var caseDataLip = caseData.getCaseDataLiP();
                HelpWithFees helpWithFees = new HelpWithFees()
                    .setHelpWithFee(YesOrNo.YES)
                    .setHelpWithFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome());
                caseData.setCaseDataLiP(caseDataLip.setHelpWithFees(helpWithFees));
                helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
            }
            if (caseData.isHWFTypeHearing()) {
                caseData.setHearingHelpFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome());
            }
            clearHwfReferenceProperties(caseData);
        }
        return caseData;
    }

    private void clearHwfReferenceProperties(CaseData caseData) {
        caseData.setFeePaymentOutcomeDetails(caseData.getFeePaymentOutcomeDetails()
                                                     .setHwfNumberAvailable(null)
                                                     .setHwfNumberForFeePaymentOutcome(null));
    }

    public CaseData updateOutstandingFee(CaseData caseData, String caseEventId) {
        BigDecimal claimIssuedRemissionAmount = NO_REMISSION_HWF == CaseEvent.valueOf(caseEventId)
            ? BigDecimal.ZERO
            : caseData.getClaimIssueRemissionAmount();
        BigDecimal hearingRemissionAmount = NO_REMISSION_HWF == CaseEvent.valueOf(caseEventId)
            ? BigDecimal.ZERO
            : caseData.getHearingRemissionAmount();
        BigDecimal claimFeeAmount = caseData.getCalculatedClaimFeeInPence();
        BigDecimal hearingFeeAmount = caseData.getCalculatedHearingFeeInPence();
        BigDecimal outstandingFeeAmount;

        if (caseData.isHWFTypeClaimIssued() && BigDecimal.ZERO.compareTo(claimFeeAmount) != 0) {
            outstandingFeeAmount = claimFeeAmount.subtract(claimIssuedRemissionAmount);
            caseData.setClaimIssuedHwfDetails(
                caseData.getClaimIssuedHwfDetails()
                    .setRemissionAmount(claimIssuedRemissionAmount)
                    .setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount))
            );
            helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
        } else if (caseData.isHWFTypeHearing() && BigDecimal.ZERO.compareTo(hearingFeeAmount) != 0) {
            outstandingFeeAmount = hearingFeeAmount.subtract(hearingRemissionAmount);
            caseData.setHearingHwfDetails(
                caseData.getHearingHwfDetails()
                    .setRemissionAmount(hearingRemissionAmount)
                    .setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount))
            );
            helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
        }
        return caseData;
    }
}
