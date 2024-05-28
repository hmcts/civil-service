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

    public void setUpHelpWithFeeTab(CaseData.CaseDataBuilder caseDataBuilder) {
        CaseData caseData = caseDataBuilder.build();
        if (FeeType.CLAIMISSUED == caseData.getHwfFeeType()) {
            caseDataBuilder.claimIssuedHwfForTab(setUpClaimIssuedHelpWithFeeTab(caseData));
        } else if (FeeType.HEARING == caseData.getHwfFeeType()) {
            caseDataBuilder.hearingHwfForTab(setUpHearingHelpWithFeeTab(caseData));
        }
    }

    private HelpWithFeesForTab setUpHearingHelpWithFeeTab(CaseData caseData) {
        return HelpWithFeesForTab.builder()
            .remissionAmount(Optional.ofNullable(caseData.getHearingHwfDetails())
                                 .map(HelpWithFeesDetails::getRemissionAmount).isPresent()
                                 ? MonetaryConversions.penniesToPounds(caseData.getHearingHwfDetails().getRemissionAmount())
                                 : null)
            .applicantMustPay(Optional.ofNullable(caseData.getHearingHwfDetails())
                                  .map(HelpWithFeesDetails::getOutstandingFeeInPounds)
                                  .orElse(null))
            .claimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedHearingFeeInPence()))
            .feeCode(caseData.getHearingFee().getCode())
            .hwfType(hearingFee)
            .hwfReferenceNumber(caseData.getHearingHelpFeesReferenceNumber())
            .build();
    }

    private HelpWithFeesForTab setUpClaimIssuedHelpWithFeeTab(CaseData caseData) {
        return HelpWithFeesForTab.builder()
            .remissionAmount(Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                                 .map(HelpWithFeesDetails::getRemissionAmount).isPresent()
                                 ? MonetaryConversions.penniesToPounds(caseData.getClaimIssuedHwfDetails().getRemissionAmount())
                                 : null)
            .applicantMustPay(Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                                  .map(HelpWithFeesDetails::getOutstandingFeeInPounds)
                                  .orElse(null))
            .claimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence()))
            .feeCode(caseData.getClaimFee().getCode())
            .hwfType(claimIssuedFee)
            .hwfReferenceNumber(caseData.getHelpWithFeesReferenceNumber())
            .build();
    }
}
