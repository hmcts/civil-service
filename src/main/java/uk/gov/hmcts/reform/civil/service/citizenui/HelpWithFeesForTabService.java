package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesForTab;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HelpWithFeesForTabService {

    private String hearingFee = "Hearing Fee";
    private String claimIssuedFee = "Claim Fee";

    public void setUpHelpWithFeeTab(CaseData caseData) {
        if (FeeType.CLAIMISSUED == caseData.getHwfFeeType()) {
            caseData.setClaimIssuedHwfForTab(setUpClaimIssuedHelpWithFeeTab(caseData));
        } else if (FeeType.HEARING == caseData.getHwfFeeType()) {
            caseData.setHearingHwfForTab(setUpHearingHelpWithFeeTab(caseData));
        }
    }

    private HelpWithFeesForTab setUpHearingHelpWithFeeTab(CaseData caseData) {
        return new HelpWithFeesForTab()
            .setRemissionAmount(Optional.ofNullable(caseData.getHearingHwfDetails())
                                 .map(HelpWithFeesDetails::getRemissionAmount).isPresent()
                                 ? MonetaryConversions.penniesToPounds(caseData.getHearingHwfDetails().getRemissionAmount())
                                 : null)
            .setApplicantMustPay(Optional.ofNullable(caseData.getHearingHwfDetails())
                                  .map(HelpWithFeesDetails::getOutstandingFeeInPounds)
                                  .orElse(null))
            .setClaimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedHearingFeeInPence()))
            .setFeeCode(caseData.getHearingFee().getCode())
            .setHwfType(hearingFee)
            .setHwfReferenceNumber(caseData.getHearingHelpFeesReferenceNumber());
    }

    private HelpWithFeesForTab setUpClaimIssuedHelpWithFeeTab(CaseData caseData) {
        return new HelpWithFeesForTab()
            .setRemissionAmount(Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                                 .map(HelpWithFeesDetails::getRemissionAmount).isPresent()
                                 ? MonetaryConversions.penniesToPounds(caseData.getClaimIssuedHwfDetails().getRemissionAmount())
                                 : null)
            .setApplicantMustPay(Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                                  .map(HelpWithFeesDetails::getOutstandingFeeInPounds)
                                  .orElse(null))
            .setClaimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence()))
            .setFeeCode(caseData.getClaimFee().getCode())
            .setHwfType(claimIssuedFee)
            .setHwfReferenceNumber(caseData.getHelpWithFeesReferenceNumber());
    }
}
