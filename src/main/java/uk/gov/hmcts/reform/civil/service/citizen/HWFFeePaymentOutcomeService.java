package uk.gov.hmcts.reform.civil.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
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
                CaseDataLiP caseDataLip = caseData.getCaseDataLiP();
                if (caseDataLip == null) {
                    caseDataLip = new CaseDataLiP();
                }
                HelpWithFees helpWithFees = new HelpWithFees()
                    .setHelpWithFee(YesOrNo.YES)
                    .setHelpWithFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome());
                caseDataLip.setHelpWithFees(helpWithFees);
                caseData.setCaseDataLiP(caseDataLip);
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
        if (caseData.getFeePaymentOutcomeDetails() != null) {
            caseData.getFeePaymentOutcomeDetails()
                .setHwfNumberAvailable(null)
                .setHwfNumberForFeePaymentOutcome(null);
        }
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
            HelpWithFeesDetails claimIssuedDetails = caseData.getClaimIssuedHwfDetails();
            if (claimIssuedDetails == null) {
                claimIssuedDetails = new HelpWithFeesDetails();
            }
            claimIssuedDetails
                .setRemissionAmount(claimIssuedRemissionAmount)
                .setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount));
            caseData.setClaimIssuedHwfDetails(claimIssuedDetails);
            helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
        } else if (caseData.isHWFTypeHearing() && BigDecimal.ZERO.compareTo(hearingFeeAmount) != 0) {
            outstandingFeeAmount = hearingFeeAmount.subtract(hearingRemissionAmount);
            HelpWithFeesDetails hearingDetails = caseData.getHearingHwfDetails();
            if (hearingDetails == null) {
                hearingDetails = new HelpWithFeesDetails();
            }
            hearingDetails
                .setRemissionAmount(hearingRemissionAmount)
                .setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount));
            caseData.setHearingHwfDetails(hearingDetails);
            helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
        }
        return caseData;
    }
}
