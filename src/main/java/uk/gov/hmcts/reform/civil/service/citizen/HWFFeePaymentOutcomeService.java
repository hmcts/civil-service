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
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        if (Objects.nonNull(caseData.getFeePaymentOutcomeDetails())
            && caseData.getFeePaymentOutcomeDetails().getHwfNumberAvailable() == YesOrNo.YES) {
            if (caseData.isHWFTypeClaimIssued()) {
                var caseDataLip = caseData.getCaseDataLiP();
                HelpWithFees helpWithFees = HelpWithFees.builder()
                    .helpWithFee(YesOrNo.YES)
                    .helpWithFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome())
                    .build();
                updatedData.caseDataLiP(caseDataLip.toBuilder().helpWithFees(helpWithFees).build());
                helpWithFeesForTabService.setUpHelpWithFeeTab(updatedData);
            }
            if (caseData.isHWFTypeHearing()) {
                updatedData.hearingHelpFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome()).build();
            }
            clearHwfReferenceProperties(updatedData);
        }
        return updatedData.build();
    }

    private void clearHwfReferenceProperties(CaseData.CaseDataBuilder caseDataBuilder) {
        CaseData caseData = caseDataBuilder.build();
        caseDataBuilder.feePaymentOutcomeDetails(caseData.getFeePaymentOutcomeDetails().toBuilder()
                                                     .hwfNumberAvailable(null)
                                                     .hwfNumberForFeePaymentOutcome(null).build());
    }

    public CaseData updateOutstandingFee(CaseData caseData, String caseEventId) {
        var updatedData = caseData.toBuilder();
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
            updatedData.claimIssuedHwfDetails(
                caseData.getClaimIssuedHwfDetails().toBuilder()
                    .remissionAmount(claimIssuedRemissionAmount)
                    .outstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount))
                    .build()
            );
            helpWithFeesForTabService.setUpHelpWithFeeTab(updatedData);
        } else if (caseData.isHWFTypeHearing() && BigDecimal.ZERO.compareTo(hearingFeeAmount) != 0) {
            outstandingFeeAmount = hearingFeeAmount.subtract(hearingRemissionAmount);
            updatedData.hearingHwfDetails(
                caseData.getHearingHwfDetails().toBuilder()
                    .remissionAmount(hearingRemissionAmount)
                    .outstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount))
                    .build()
            );
            helpWithFeesForTabService.setUpHelpWithFeeTab(updatedData);
        }
        return updatedData.build();
    }
}
